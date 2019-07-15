package es.coffeebyt.wtu.voucher;

import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

public class VoucherTest {

    private final List<String> sample = asList(
            "wtubtc-1ca47211-686a-4d32-9180-b9a8895f97ac",
            "wtubtc-924eb4a2-c943-49e7-aa10-5eeceb6afcaf",
            "wtubtc-fd015f96-4e0c-47fc-812a-57e414e02acb",
            "wtubtc-380b6e9b-9554-45e9-aaaf-ae79ba5af8a4",
            "wtubtc-3dbd5324-44bc-4c45-a7a9-31946614cf3c",
            "wtubtc-a8abce2e-b239-4abe-a68b-0c7b6aea548c",
            "wtubtc-baa19363-7cc4-4120-9564-9457e4668d7d",
            "wtubtc-4aed0365-2bad-41f0-b85e-d43ec66d5a2a",
            "wtubtc-1a564c73-53f5-445f-9708-857c1378c96b",
            "wtubtc-3e37644b-58b6-4189-9d3e-a427dfd001cf",
            "wtubtc-5f74c9a3-b4c6-407c-b7e0-5762b2d22593",
            "wtubtc-2fc1b2c9-8b46-4015-89df-d9d525dd5a22",
            "wtubtc-c451c864-a3a4-4617-8edb-1d85320be0ca",
            "wtubtc-5fc882c4-1267-4953-a65a-675d31217b20",
            "wtubtc-c168d3b1-9dac-4180-9e4b-3127f082de68",
            "wtubtc-839d66ea-0341-4300-8587-1bd16f3c00e2",
            "wtubtc-2f7db4b8-7f09-4b10-b149-9822407de99d",
            "wtubtc-dbca415c-162d-4b2d-a838-2db54e092a1a",
            "wtubtc-edb31bc2-9b97-4b28-bea0-1e96bfccf125",
            "wtubtc-dff30255-b888-412e-a80a-fe7367197cdc",
            "wtubtc-75d80a6e-428e-4633-a43f-98612a79e779",
            "wtubtc-6b6220af-37a9-4f82-87a4-8156646bb8f9",
            "wtubtc-341f5e9b-e7fb-45c9-8320-1ea9cb807bb2",
            "wtubtc-8fb9458d-d6f5-4301-b8ee-c6e33ce8e02e",
            "wtubtc-9f5c7abb-4735-4131-a660-b700011d9dc4",
            "wtubtc-0e2f783e-f08c-4fda-ae8d-2fba8bcc3e7c",
            "wtubtc-8e5ee8ef-72fe-4f8a-9e84-4af0da664187",
            "wtubtc-7a779794-cf07-4263-a540-478e1dac4e26",
            "wtubtc-f0c2ddd8-b4da-430f-8850-2999d50d55dd",
            "wtubtc-838fd1f2-a9c9-466d-89dd-4df61e870bf0",
            "wtubtc-9dcfb33e-5dd4-452a-8eb2-badcfe2757ca",
            "wtubtc-27b35bbc-afef-4606-83af-a34aad238dd7",
            "wtubtc-835a68b1-a868-43ed-b4f2-e75dc1d1fe89",
            "wtubtc-c523db0f-cc0b-4b6b-a1f0-53379b8432a2",
            "wtubtc-dbbfa320-5d18-4e61-b1fb-96c260f1d0bd",
            "wtubtc-4aa0d06c-cc62-499b-b4e4-b0d96386623d",
            "wtubtc-08ea36b7-e164-49bd-adc7-24db716589b7",
            "wtubtc-5f5f2622-47b5-4c19-ba1f-ec50b2cf3a0e",
            "wtubtc-15692fc8-5e9c-46f4-89d4-6af02f4fcdee",
            "wtubtc-010644e2-79a2-4e34-b15a-e86cbeaedfd9",
            "wtubtc-b19ccf29-18bd-45ca-8df6-674ed7cf6e2a",
            "wtubtc-61cfc002-7929-4f82-8da4-dcfe269bd7a1",
            "wtubtc-85d1d743-336d-4074-8dcc-b0d5059bfbab",
            "wtubtc-c3b91f66-7736-43a4-a725-66362e30a63b",
            "wtubtc-684f1c9c-d236-4146-a130-850240cad0d8",
            "wtubtc-8503e5cd-3383-448c-9d54-63a5378e7d15",
            "wtubtc-cc86e016-fcb9-4901-82ba-64937e664053",
            "wtubtc-adfecefa-5fdd-4ad2-9bbd-f32483a0ca43",
            "wtubtc-4f16800d-8ca9-42ea-8d22-9b8a3c96df82",
            "wtubtc-ae584164-e2e3-45b0-b650-7888f1ea1cc8",
            "wtubtc-e0b404e2-0a2c-42ff-8bdf-2b542ffad687",
            "wtubtc-dfce1c43-2480-4618-8917-9b4bbd78e896",
            "wtubtc-92b0012b-85f6-4663-84c9-bd66fbae8225",
            "wtubtc-0c2c8c2a-fd7a-45cb-b42f-7f56760b52cf",
            "wtubtc-ab7cc504-bc1d-4bd2-8df2-34ec19336b1f",
            "wtubtc-91340694-b01a-477f-8896-420dc108e9dc",
            "wtubtc-c4e9846b-a154-4755-a283-96c3e8b7e3ca",
            "wtubtc-667e0b61-a55d-45af-822e-bbc7a5bf3b53",
            "wtubtc-e917b8f2-54c3-4dfd-a4a1-e9e857afd7cc",
            "wtubtc-5b06ef5d-c5f6-4070-bd48-6b256862dba6",
            "wtubtc-c26b92b9-4560-41d4-bbc1-1b9fda007d36",
            "wtubtc-d8a467c2-3193-4ee8-9f7f-8b3e595ce5a0",
            "wtubtc-eea9e89e-44b4-4881-a3c8-9083a8ccbd31",
            "wtubtc-1533fc69-ea5c-478d-9a6b-ba0048fccbb3",
            "wtubtc-66513522-63d8-4c3f-9132-0b995639e050",
            "wtubtc-0735c258-d3cd-47a6-9706-0a18aa3ce3f4",
            "wtubtc-2602454d-fd9a-4afc-a377-84209a3a15b8",
            "wtubtc-95a9779c-48f1-429a-ab4d-73a99b46f419",
            "wtubtc-da416176-3c43-46d8-91cf-174548dc7d68",
            "wtubtc-93b1736f-a4e1-49bc-b0cf-cce65dd9a3f1",
            "wtubtc-edeb9858-c141-4555-85b1-46a8dbd16bc1",
            "wtubtc-47c490a8-09cb-4969-814b-8d723d559035",
            "wtubtc-68d74dd5-8c1a-482b-bdd7-fd6ee4db4c9b",
            "wtubtc-0d547127-aef3-41d7-aabb-286c24c0bc10",
            "wtubtc-1119e83c-16dd-4d96-b418-88453e5198c5",
            "wtubtc-e5de8b78-558d-4da5-9c1e-59694f7052c0",
            "wtubtc-75685c3a-2b62-4844-b59c-4a7d29bafbb6",
            "wtubtc-dcfde93e-1dae-4b9e-ac7b-4347aa289212",
            "wtubtc-1970d0bc-59b1-4779-aeb9-8b1a32181198",
            "wtubtc-d9ba1ca7-02ef-4c11-ba93-b79ad8d54348",
            "wtubtc-936a0bc5-0941-4b11-93ac-fcc5e30a7fab",
            "wtubtc-b25632f0-f5c7-4b47-9fe3-44b4755f527a",
            "wtubtc-2dee667c-3919-42ee-a033-a970f46cbcc4",
            "wtubtc-0ad0698c-a770-4df1-a6db-ac44678a1f12",
            "wtubtc-45a298d7-ca90-4a76-b27b-aff201303bef",
            "wtubtc-79e1a096-45a2-4742-8d88-256f3612624f",
            "wtubtc-e41105f8-49b4-4521-bbba-d56ef048658a",
            "wtubtc-b7133e32-6d42-4c77-b2e9-56b41f90835e",
            "wtubtc-4f7d3712-a6d7-4656-9308-2098f7924235",
            "wtubtc-97447c1a-b805-4d17-9be1-90d84b2e1ac6"
    );

    @Test
    public void testCodePattern() {
        Pattern pattern = Pattern.compile(Voucher.CODE_PATTERN);
        sample.forEach(code -> assertTrue(pattern.matcher(code).matches()));
    }
}