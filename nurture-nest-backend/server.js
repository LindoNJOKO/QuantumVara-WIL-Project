const express = require("express");
const Stripe = require("stripe");
const cors = require("cors");
require("dotenv").config();

const app = express();
app.use(cors());
app.use(express.json());

// ✅ Initialize Stripe with secret key
const stripe = Stripe(process.env.STRIPE_SECRET_KEY);

// Create a PaymentIntent
app.post("/create-payment-intent", async (req, res) => {
  try {
    const { amount } = req.body; // amount in cents

    const paymentIntent = await stripe.paymentIntents.create({
      amount: amount,
      currency: "zar", // change if needed
      automatic_payment_methods: { enabled: true },
    });

    res.json({
      clientSecret: paymentIntent.client_secret,
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

// ✅ Test route
app.get("/", (req, res) => {
  res.send("Stripe backend is running!");
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
