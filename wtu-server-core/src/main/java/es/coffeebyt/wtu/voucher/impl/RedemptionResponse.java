package es.coffeebyt.wtu.voucher.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Wither
public class RedemptionResponse implements Serializable {

    @NotEmpty
    List<String> trackingUrls;

    @NotBlank
    String transactionId;
}
