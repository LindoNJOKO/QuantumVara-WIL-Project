const express = require("express");
const Stripe = require("stripe");
const cors = require("cors");
require("dotenv").config();

const app = express();
app.use(cors());
app.use(express.json());

const stripe = Stripe(process.env.STRIPE_SECRET_KEY);

app.get("/", (req, res) => {
  res.send("Payment server is running!");
});

app.post("/create-payment-intent", async (req, res) => {
  try {
    const { amount } = req.body;

    console.log(`Payment request: R${amount / 100}`);

    const paymentIntent = await stripe.paymentIntents.create({
      amount: amount,
      currency: "zar",
      automatic_payment_methods: {
        enabled: true,
      },
    });

    console.log("Payment created:", paymentIntent.id);

    res.json({
      clientSecret: paymentIntent.client_secret,
    });
  } catch (error) {
    console.error("Error:", error.message);
    res.status(500).json({ error: error.message });
  }
});

// Use Railway's PORT or default to 5000
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`✅ Server running on port ${PORT}`);
});
