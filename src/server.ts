import express from 'express';

const hostname = 'localhost';
const port = 3000;
const app = express();

var shopifyAPI = require('shopify-node-api');

var Shopify = new shopifyAPI({
    shop: 'prepaid-cards-poc', // MYSHOP.myshopify.com
    shopify_api_key: '01f2aa884f9495a34407c622eba388f5', // Your API key
    access_token: 'a81786928790c4d5c2d31a0bfaf9ed9e' // Your API password
});

function callback(err: any, data: any, headers: any) {
    var api_limit = headers['http_x_shopify_shop_api_call_limit'];
    console.log(api_limit); // "1/40"
}

app.get('/', (req, res) => {
    res.send('Hello World!');
});

app.get('/api/', (req, res, next) => {
    res.statusCode = 200;
    res.setHeader('Content-Type', 'text/plain');
    res.end('Hello World');
});

app.get('/api/send_voucher_to_shopify', (req, res, next) => {

    // Shopify.get('/admin/products.json', function(err:any, data:any, headers:any){
    //     console.log(data); // Data contains product json information
    //     console.log(headers); // Headers returned from request
    //   });

    Shopify.post('/admin/inventory_levels/set.json',
        {
            location_id: 10255499328,
            inventory_item_id: 16019805634624,
            available: 2000
        },
        function (err: any, data: any, headers: any) {
            console.log("Vouchers stock updated.");
        })
        ;
});

app.listen(port, hostname, () => {
    // connect to the DB
    console.log(`Server running at http://${hostname}:${port}/`);
});
