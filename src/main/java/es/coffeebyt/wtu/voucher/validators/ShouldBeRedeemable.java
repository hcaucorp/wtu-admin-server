package es.coffeebyt.wtu.voucher.validators;

import es.coffeebyt.wtu.exception.Thrower;
import es.coffeebyt.wtu.repository.VoucherRepository;
import es.coffeebyt.wtu.voucher.RedemptionValidator;
import es.coffeebyt.wtu.voucher.Voucher;
import es.coffeebyt.wtu.voucher.VoucherNotFoundException;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
    }
}
