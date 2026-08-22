# Deferred Admin/Inspector Endpoints

## Overview
As per requirements, the following endpoints depend on admin and inspector workflows and are deferred for future implementation. These endpoints handle the middle stages of the property application workflow after the owner submits their application.

---

## Inspector Workflow Endpoints (Deferred)

### InspectorController
**Base Path**: `/api/inspections`
**Authentication**: Requires `@RequiresRole(UserRole.INSPECTOR)` annotation

#### 1. Confirm Inspection Slot Selection
**Endpoint**: `POST /api/inspections/schedules/{scheduleId}/confirm`

**Purpose**: Inspector confirms which proposed time slot they will use for the inspection

**Request Body**:
```json
{
  "exactTime": "2026-06-05T10:00:00Z",
  "notes": "Will need building access card from reception",
  "inspectorPreferences": ["morning", "ground-floor"]
}
```

**Response**: InspectionScheduleResponse with status = CONFIRMED

**Business Logic**:
- Update InspectionSchedule.exactTime
- Update InspectionSchedule.status = CONFIRMED
- Update PropertyApplication.status = INSPECTION_SCHEDULED
- Trigger notification to owner

**Status**: 📋 Not Started

---

#### 2. Submit Inspection Report
**Endpoint**: `POST /api/inspections/schedules/{scheduleId}/report`

**Purpose**: Inspector submits findings and property assessment after visiting

**Request Body**:
```json
{
  "actualVisitTime": "2026-06-05T10:30:00Z",
  "roomsCount": 3,
  "areaSqm": 148.5,
  "furnishing": "SEMI_FURNISHED",
  "conditionRating": "GOOD",
  "photos": [
    "https://s3.example.com/inspection/photo1.jpg",
    "https://s3.example.com/inspection/photo2.jpg"
  ],
  "comments": "Property in excellent condition. Some minor paint needed in bedroom.",
  "recommendedRent": 2350.00,
  "violations": []
}
```

**Response**: InspectionReportResponse

**Business Logic**:
- Create InspectionReport record
- Update PropertyDetail with inspector findings
- Update InspectionSchedule.status = IN_PROGRESS → COMPLETED
- Update PropertyApplication.status = PENDING_ADMIN_DECISION
- Trigger notification to admin for review

**Status**: 📋 Not Started

---

#### 3. List Assigned Inspections
**Endpoint**: `GET /api/inspections/assigned`

**Purpose**: Inspector views their assigned property applications pending inspection

**Query Parameters**:
- `status`: PENDING_INSPECTOR_SLOTS, INSPECTION_SCHEDULED (optional)
- `page`: 0 (default)
- `size`: 10 (default)

**Response**: Paginated list of InspectionAssignmentResponse

**Status**: 📋 Not Started

---

#### 4. Get Inspection Details
**Endpoint**: `GET /api/inspections/schedules/{scheduleId}`

**Purpose**: Inspector reviews details of a specific inspection assignment

**Response**: InspectionScheduleDetailResponse including:
- Proposed time slots
- Property details
- Owner contact info
- Property location/directions
- Assigned report (if exists)

**Status**: 📋 Not Started

---

## Admin Workflow Endpoints (Deferred)

### AdminController
**Base Path**: `/api/admin`
**Authentication**: Requires `@RequiresRole(UserRole.ADMIN)` annotation

#### 1. Assign Inspector to Application
**Endpoint**: `POST /api/admin/applications/{applicationId}/assign-inspector`

**Purpose**: Admin assigns an available inspector to a property application

**Request Body**:
```json
{
  "inspectorId": 5,
  "assignmentNotes": "Preferred inspector for upscale properties"
}
```

**Response**: PropertyApplicationResponse

**Business Logic**:
- Verify application status is SUBMITTED
- Verify inspector exists and is available
- Update PropertyApplication.inspector_id
- Update PropertyApplication.status = PENDING_INSPECTOR_SLOTS
- Create audit log entry
- Trigger notification to owner: "Inspector assigned, please provide availability windows"
- Trigger notification to inspector: "New inspection assigned"

**Status**: 📋 Not Started

---

#### 2. Submit Admin Decision
**Endpoint**: `POST /api/admin/applications/{applicationId}/decision`

**Purpose**: Admin reviews inspection report and approves/rejects/holds the application

**Request Body**:
```json
{
  "inspectionReportId": 12,
  "decision": "APPROVED",
  "decidedRent": 2400.00,
  "decisionNotes": "Property meets all requirements. Good condition.",
  "commissionPercentage": 5.0
}
```

**Decision Options**:
- `APPROVED`: Property accepted, owner proceeds to consent
- `REJECTED`: Property not accepted, workflow ends
- `HOLD`: Property under review, awaiting more info

**Response**: AdminDecisionResponse

**Business Logic**:
- Create AdminDecision record
- If APPROVED: Update PropertyApplication.status = PENDING_OWNER_CONSENT
- If REJECTED: Update PropertyApplication.status = REJECTED_BY_ADMIN
- If HOLD: Keep status, update with hold reason
- Trigger notification to owner and inspector
- Store decision details for audit trail

**Status**: 📋 Not Started

---

#### 3. Activate Listing
**Endpoint**: `POST /api/admin/listings/activate`

**Purpose**: Admin creates an active listing once owner has provided consent

**Request Body**:
```json
{
  "applicationId": 1,
  "termsConsentId": 1,
  "adminDecisionId": 1,
  "listingTitle": "Spacious 3BR Semi-Furnished Apartment",
  "listingDescription": "Beautiful apartment in Cairo with modern amenities",
  "listingStatus": "ACTIVE"
}
```

**Response**: ListingResponse

**Business Logic**:
- Verify ApplicationStatus = CONSENT_PROVIDED
- Verify TermsConsent exists
- Verify AdminDecision approved
- Create Listing record with ACTIVE status
- Update PropertyApplication.status = ACTIVE
- Add listing to search/discovery system
- Trigger notification to owner: "Your listing is now live!"
- Create notification to potential tenants (async)

**Status**: 📋 Not Started

---

#### 4. List Applications for Review
**Endpoint**: `GET /api/admin/applications`

**Purpose**: Admin views all applications requiring action

**Query Parameters**:
- `status`: SUBMITTED, PENDING_ADMIN_DECISION, HOLD (optional)
- `priority`: LOW, MEDIUM, HIGH (optional)
- `inspectorId`: Filter by assigned inspector (optional)
- `page`, `size`: Pagination

**Response**: Paginated list of PropertyApplicationAdminView

**Status**: 📋 Not Started

---

#### 5. Get Application Review Details
**Endpoint**: `GET /api/admin/applications/{applicationId}/review`

**Purpose**: Admin reviews complete application including inspection report

**Response**: ApplicationReviewResponse including:
- Application details
- Property details
- Owner info
- Inspection report (if completed)
- Previous decisions/holds (if any)
- Recommended rent and violations

**Status**: 📋 Not Started

---

#### 6. Get Inspector Statistics
**Endpoint**: `GET /api/admin/inspectors/{inspectorId}/statistics`

**Purpose**: Admin views inspector performance metrics

**Response**: InspectorStatsResponse
```json
{
  "inspectorId": 5,
  "name": "Ahmed Hassan",
  "totalAssigned": 45,
  "totalCompleted": 42,
  "approvalRate": 88.5,
  "averageTimeToComplete": "5.2 days",
  "averageRecommendedRent": 2450.00,
  "lastActivityAt": "2026-06-02T10:00:00Z"
}
```

**Status**: 📋 Not Started

---

## Data Entities Required

### InspectionReport Entity (Already Created)
```
- id: Long (PK)
- schedule_id: Long (FK → InspectionSchedule)
- inspector_id: Long (FK → AuthUser)
- property_detail_id: Long (FK → PropertyDetail)
- actual_visit_time: Instant
- condition_rating: Enum (POOR, FAIR, GOOD, EXCELLENT)
- comments: Text
- recommended_rent: Decimal(10,2)
- photos: JSON array
- violations: JSON array
- created_at: Instant
- updated_at: Instant
```

### AdminDecision Entity (Already Created)
```
- id: Long (PK)
- application_id: Long (FK → PropertyApplication)
- admin_id: Long (FK → AuthUser)
- report_id: Long (FK → InspectionReport)
- decision: Enum (APPROVED, REJECTED, HOLD)
- decided_rent: Decimal(10,2)
- commission_percentage: Decimal(5,2)
- decision_notes: Text
- decided_at: Instant
- created_at: Instant
- updated_at: Instant
```

### Listing Entity (Already Created)
```
- id: Long (PK)
- property_detail_id: Long (FK → PropertyDetail)
- owner_id: Long (FK → AuthUser)
- application_id: Long (FK → PropertyApplication)
- decision_id: Long (FK → AdminDecision)
- title: String
- description: Text
- status: Enum (ACTIVE, SUSPENDED, ARCHIVED)
- activated_at: Instant
- created_at: Instant
- updated_at: Instant
```

---

## Implementation Roadmap

### Phase 1: Inspector Workflow (Priority 1)
- [ ] InspectorController creation
- [ ] Inspection schedule confirmation logic
- [ ] Inspection report submission
- [ ] Inspector assignment list view
- [ ] InspectorService with business logic

### Phase 2: Admin Workflow (Priority 1)
- [ ] AdminController creation
- [ ] Inspector assignment logic
- [ ] Admin decision submission
- [ ] Listing activation
- [ ] AdminService with business logic

### Phase 3: Admin Dashboard (Priority 2)
- [ ] Statistics endpoints
- [ ] Analytics queries
- [ ] Performance metrics

### Phase 4: Notifications & Audit (Priority 2)
- [ ] Event-driven notifications
- [ ] Audit logging
- [ ] Activity tracking

---

## Security Requirements

All deferred endpoints must:
1. ✅ Require JWT authentication (Bearer token)
2. ✅ Use `@RequiresRole` annotations for role-based access
3. ✅ Validate authorization on each resource
4. ✅ Log audit trails for admin actions
5. ✅ Use appropriate HTTP status codes (400, 403, 404, 409)
6. ✅ Validate input DTOs with `@Valid` annotations

---

## Testing Strategy

### Unit Tests
- Service layer business logic
- Authorization checks
- Status transition validation

### Integration Tests
- End-to-end workflow across all roles
- Database transaction handling
- Notification trigger verification

### E2E Tests
- Complete application from submission to listing
- Inspector and admin workflows
- Error scenarios and edge cases

---

## Estimated Effort

- **InspectorController**: 2-3 days
- **AdminController**: 2-3 days
- **Service Layer**: 2-3 days
- **Testing**: 2-3 days
- **Documentation**: 1 day
- **Total**: ~10-13 days

---

## Related Issues & Dependencies

1. **Notification System**: Required for workflow notifications
2. **Email Service**: For sending updates to owners/inspectors/admins
3. **Audit Logging**: For tracking admin decisions
4. **Search/Discovery**: For making listings discoverable to tenants
5. **Payment Integration**: For collecting commission on bookings

---

**Last Updated**: 2026-06-02
**Status**: 📋 Deferred for Future Implementation
**Owner**: Backend Team
