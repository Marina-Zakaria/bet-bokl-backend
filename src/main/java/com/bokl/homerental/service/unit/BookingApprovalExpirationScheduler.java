package com.bokl.homerental.service.unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BookingApprovalExpirationScheduler {
    private static final Logger log =
            LoggerFactory.getLogger(BookingApprovalExpirationScheduler.class);

    private final BookingService bookingService;

    public BookingApprovalExpirationScheduler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** Runs daily at 00:05 in the server time zone. */
    @Scheduled(cron = "${booking.approval-expiration-cron:0 5 0 * * *}")
    public void expirePendingOwnerApprovals() {
        int count = bookingService.expirePendingApprovals();
        if (count > 0) {
            log.info("Expired {} pending owner booking approvals", count);
        }
    }
}
