package es.coffeebyt.wtu.voucher.listeners;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

import es.coffeebyt.wtu.exception.Thrower;
import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.voucher.RedemptionValidator;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.VoucherNotFoundException;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShouldBeRedeemable implements RedemptionValidator {

    private final VoucherRepository voucherRepository;

    @Override
    public void validate(RedemptionRequest redemptionRequest) {
        Voucher voucher = voucherRepository.findByCode(redemptionRequest.getVoucherCode())
                .orElseThrow(() -> new VoucherNotFoundException("Voucher " + redemptionRequest.getVoucherCode() + " not found."));

        Objects.requireNonNull(voucher, "voucher");

        if (!voucher.isPublished())
            Thrower.logAndThrowIllegalOperationException("Attempting to redeem unpublished voucher " + voucher.getCode());

        if (voucher.isRedeemed())
            Thrower.logAndThrowIllegalOperationException("Attempting to redeem already redeemed voucher " + voucher.getCode());

        if (isExpired(voucher))
            Thrower.logAndThrowIllegalOperationException("Attempting to redeem expired voucher " + voucher.getCode());
    }

    private boolean isExpired(Voucher voucher) {
        return Instant.now().isAfter(Instant.ofEpochMilli(voucher.getExpiresAt()));
    }
}
