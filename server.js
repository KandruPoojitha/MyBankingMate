const express = require("express");
const stripe = require("stripe")("sk_test_51PlVh8P9Bz7XrwZPWSkDzX7AmaNgVr04yPOQWnbAECiYSWKtsmmVgD2Z8JYBY8a5dmEfKXaTewrBESb3fxIliwDo00HdJmKBKz");
const cors = require("cors");

const app = express();
app.use(cors());
app.use(express.json());

app.post("/create-payment-intent", async (req, res) => {
    const { amount } = req.body;

    try {
        const paymentIntent = await stripe.paymentIntents.create({
            amount,
            currency: "usd",
            payment_method_types: ["card"],
        });

        res.send({
            clientSecret: paymentIntent.client_secret,
        });
    } catch (error) {
        console.error(error);
        res.status(500).send({ error: error.message });
    }
});

const PORT = 4242;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
