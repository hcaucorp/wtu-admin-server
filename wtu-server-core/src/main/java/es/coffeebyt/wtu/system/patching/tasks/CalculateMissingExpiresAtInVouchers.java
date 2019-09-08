package es.coffeebyt.wtu.system.patching.tasks;

import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.system.patching.PatchingResult;
import es.coffeebyt.wtu.system.patching.PatchingTask;
import es.coffeebyt.wtu.voucher.Voucher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static es.coffeebyt.wtu.time.TimeStamp.clearTimeInformation;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class CalculateMissingExpiresAtInVouchers implements PatchingTask {

    private final VoucherRepository voucherRepository;

    /**
     * - migrate old voucher expiration dates with priority:
     * - Malta vouchers: expire on 1st December 2019.
     * - Purchased ones: expire in 2 years from now.
     * - Published ones: expire in 2 years from now.
     */
    @Override public List<PatchingResult> call() {
        List<Voucher> correctedVouchers = voucherRepository.findByPublishedTrue().stream()
                .filter(voucher -> voucher.getExpiresAt() == 0)
                .map(voucher -> voucher.withExpiresAt(twoYearsFromNowMillis()))
                .collect(toList());

        voucherRepository.saveAll(correctedVouchers);

        return correctedVouchers.stream()
                .map(voucher -> new PatchingResult(
                        "Voucher.expiresAt",
                        voucher.getCode(),
                        "Updated missing 'expiresAt' value to " + voucher.getExpiresAt())
                )
        .collect(toList());
    }

    /**
     * Creates time in millis but hour/minutes/seconds is gone, 0.
     */
    private long twoYearsFromNowMillis() {
        return clearTimeInformation(
                ZonedDateTime.now(ZoneOffset.UTC).plusYears(2).toInstant().toEpochMilli()
        );
    }
}
