# Staging deployment on Ubuntu EC2

This setup deploys every successful push to `staging`. GitHub Actions tests the
backend, connects to EC2 through SSH, fast-forwards the private repository, and
rebuilds the Docker Compose application.

PostgreSQL runs in a private Docker network on the same EC2 instance. Only
Nginx ports 80/443 are public; the Spring application binds to
`127.0.0.1:8080`, and PostgreSQL has no host port.

## Required values

- EC2 public DNS name or Elastic IP
- API domain, for example `api.staging.example.com`
- Email address for Let's Encrypt expiration notices
- Existing EC2 SSH private key
- A read-only GitHub deploy key for EC2
- A frontend origin for CORS

## 1. EC2 and DNS

Use Ubuntu 24.04 LTS with at least 2 GB RAM and 20 GB EBS for staging.

Allow inbound traffic in the EC2 security group:

- TCP 22 from your administration IP
- TCP 80 from the internet
- TCP 443 from the internet

Do not expose ports 5432 or 8080.

Assign an Elastic IP. Point the API domain's `A` record to that address before
requesting the TLS certificate.

## 2. Install host dependencies

Run once on EC2:

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl git nginx certbot python3-certbot-nginx
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu
sudo systemctl enable --now docker nginx
```

Sign out and reconnect so the Docker group takes effect.

## 3. Give EC2 read-only repository access

Generate a dedicated key on EC2:

```bash
ssh-keygen -t ed25519 -f ~/.ssh/github_bokl -C "bokl-staging-ec2" -N ""
cat ~/.ssh/github_bokl.pub
```

Add the public key in the GitHub repository under **Settings → Deploy keys**.
Do not enable write access.

Configure EC2 to use it:

```bash
cat >> ~/.ssh/config <<'EOF'
Host github.com
  HostName github.com
  User git
  IdentityFile ~/.ssh/github_bokl
  IdentitiesOnly yes
EOF
chmod 600 ~/.ssh/config
ssh -T git@github.com
```

Clone the staging branch:

```bash
sudo mkdir -p /opt/bokl
sudo chown ubuntu:ubuntu /opt/bokl
git clone --branch staging \
  git@github.com:Marina-Zakaria/bet-bokl-backend.git \
  /opt/bokl/home-rental-service
cd /opt/bokl/home-rental-service
```

## 4. Create EC2 runtime secrets

```bash
cp .env.staging.example .env.staging
chmod 600 .env.staging
```

Generate a JWT secret:

```bash
openssl rand -base64 32
```

Generate persistent RSA keys:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out rsa_private.pem
openssl pkcs8 -topk8 -nocrypt -in rsa_private.pem -outform DER | base64 -w0
openssl pkey -in rsa_private.pem -pubout -outform DER | base64 -w0
rm rsa_private.pem
```

Put the generated values and a strong PostgreSQL password in `.env.staging`.
Set `CORS_ALLOWED_ORIGIN_PATTERNS` to the exact HTTPS frontend origin.

Never commit `.env.staging`.

## 5. Start the first deployment manually

```bash
docker compose --env-file .env.staging \
  -f docker-compose.staging.yml \
  up -d --build

curl --fail http://127.0.0.1:8080/health
```

Liquibase creates the database schema. The staging service runs with
`SPRING_PROFILES_ACTIVE=prod,staging`, which applies production security/error
settings but temporarily fixes OTP to `111111`. Test admin/inspector accounts
are excluded from this profile.

The stub OTP sender is available to local/staging only. Starting the pure
`prod` profile intentionally fails until a real `OtpSender` implementation
(such as AWS SNS) is added, preventing OTP values from being logged in
production.

## 6. Configure Nginx and HTTPS

Replace the domain placeholder and enable the site:

```bash
sudo sed 's/__API_DOMAIN__/api.staging.example.com/g' \
  deploy/nginx/bokl-api.conf.template \
  | sudo tee /etc/nginx/sites-available/bokl-api >/dev/null
sudo ln -sfn /etc/nginx/sites-available/bokl-api /etc/nginx/sites-enabled/bokl-api
sudo nginx -t
sudo systemctl reload nginx
sudo certbot --nginx -d api.staging.example.com --redirect
```

Verify:

```bash
curl --fail https://api.staging.example.com/health
```

## 7. Configure GitHub Actions secrets

Create a GitHub **staging environment**, then add:

- `EC2_HOST`: EC2 public DNS or Elastic IP
- `EC2_USER`: `ubuntu`
- `EC2_APP_PATH`: `/opt/bokl/home-rental-service`
- `EC2_SSH_PRIVATE_KEY`: the complete private EC2 SSH key
- `EC2_KNOWN_HOSTS`: verified SSH host-key entry for the EC2 host

Generate the known-host entry from a trusted machine:

```bash
ssh-keyscan -H YOUR_EC2_HOST
```

Compare its fingerprint with the host before saving it:

```bash
ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub
```

The workflow never stores database/JWT/RSA secrets in GitHub. Those remain in
the EC2-only `.env.staging`.

## 8. Deployment flow

1. Open a pull request from `develop` to `staging`.
2. CI tests the pull request.
3. Merge it into `staging`.
4. `deploy-staging.yml` tests again, connects to EC2, runs a fast-forward pull,
   rebuilds the image, starts the containers, and verifies `/health`.

Protect `develop` and `staging` in GitHub and require the CI check before merge.

## Database durability

The named volume `bokl-staging-postgres-data` survives container recreation and
`docker compose down`. Never run `docker compose down -v`.

Because the database and backend share one EC2 instance, enable EBS snapshots
and schedule off-instance `pg_dump` backups to S3 before storing important data.
For production traffic, migrate PostgreSQL to RDS.
