package es.coffeebyt.wtu.system.patching.tasks;

import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.system.patching.PatchingResult;
import es.coffeebyt.wtu.system.patching.PatchingTask;
import es.coffeebyt.wtu.voucher.Voucher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static es.coffeebyt.wtu.voucher.listeners.MaltaPromotion.EXPIRATION_TIME;
import static es.coffeebyt.wtu.voucher.listeners.MaltaPromotion.MALTA_VOUCHER_SKU;
import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toList;

/**
 * //TODO: Run this patch after publishing vouchers with sku: AI_AND_BC_SUMMIT_WINTER_EDITION_PROMOTIONAL_VOUCHER
 * //TODO: Running it before will have no effect because voucher codes must be published. Publishing by default
 * //TODO: will set expiration date in 2 years but this will shorten it and set 1st December 2019 as limit.
 */
@Component
@RequiredArgsConstructor
public class CalculateExpiresAtForMaltaVouchers implements PatchingTask {

    private final VoucherRepository voucherRepository;

    @Override
    public List<PatchingResult> call() {
        List<Voucher> correctedVouchers = voucherRepository.findByPublishedTrue().stream()
                .filter(voucher -> MALTA_VOUCHER_SKU.equals(voucher.getSku()))
                .map(voucher -> voucher.withExpiresAt(EXPIRATION_TIME))
                .collect(toList());

        voucherRepository.saveAll(correctedVouchers);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss Z");
        String message = format("Set expiration date of Malta promotional Vouchers to: %d (%s)",
                EXPIRATION_TIME,
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(EXPIRATION_TIME), UTC).format(formatter)
        );

        return correctedVouchers.stream()
                .map(voucher -> new PatchingResult(
                        "Voucher.expiresAt",
                        format("Code: %s, sku: %s", voucher.getCode(), voucher.getSku()),
                        message
                ))
                .collect(toList());
    }
}
