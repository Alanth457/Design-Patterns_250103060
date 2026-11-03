public class Task4Runner {
    // Old order stores its price in GBP.
    static class LegacyForeignOrder {
        String id;
        double gbp;
        LegacyForeignOrder(String id, double gbp) { this.id = id; this.gbp = gbp; }
    }

    static class LegacyCurrencyConverter {
        double convert(double amount, String from, String to) {
            if (from.equals("GBP") && to.equals("USD")) return amount * 1.25;
            return amount;
        }
    }

    interface PricingService {
        double calculateTotal();
        String getReceiptBreakdown();
    }

    // Adapter converts the old GBP order into USD pricing.
    static class CurrencyOrderAdapter implements PricingService {
        private LegacyForeignOrder order;
        private LegacyCurrencyConverter converter;
        CurrencyOrderAdapter(LegacyForeignOrder order, LegacyCurrencyConverter converter) {
            this.order = order;
            this.converter = converter;
        }
        public double calculateTotal() {
            return converter.convert(order.gbp, "GBP", "USD");
        }
        public String getReceiptBreakdown() {
            return String.format("Order [%s] Base Item: $%.2f USD (Converted from %.2f GBP)",
                    order.id, calculateTotal(), order.gbp);
        }
    }

    // Decorator adds extra fees to the price.
    static abstract class PricingDecorator implements PricingService {
        protected PricingService service;
        PricingDecorator(PricingService service) { this.service = service; }
        public String getReceiptBreakdown() { return service.getReceiptBreakdown(); }
    }

    static class GiftWrapDecorator extends PricingDecorator {
        GiftWrapDecorator(PricingService s) { super(s); }
        public double calculateTotal() { return service.calculateTotal() + 5; }
        public String getReceiptBreakdown() { return super.getReceiptBreakdown() + " + [Gift Wrap: $5.00]"; }
    }

    static class ExpressCourierDecorator extends PricingDecorator {
        ExpressCourierDecorator(PricingService s) { super(s); }
        public double calculateTotal() { return service.calculateTotal() + 15; }
        public String getReceiptBreakdown() { return super.getReceiptBreakdown() + " + [Express Courier: $15.00]"; }
    }

    static class CustomsDutyDecorator extends PricingDecorator {
        CustomsDutyDecorator(PricingService s) { super(s); }
        public double calculateTotal() {
            return Math.round(service.calculateTotal() * 1.085 * 100) / 100.0;
        }
        public String getReceiptBreakdown() { return super.getReceiptBreakdown() + " + [Customs Duty: 8.5%]"; }
    }

    public static void main(String[] args) {
        PricingService base = new CurrencyOrderAdapter(
                new LegacyForeignOrder("ORD-9821", 100), new LegacyCurrencyConverter());

        PricingService checkout = new CustomsDutyDecorator(
                new ExpressCourierDecorator(new GiftWrapDecorator(base)));

        System.out.println(">>> TASK 4: ADAPTER + DECORATOR <<<");
        System.out.printf("Base Converted: $%.2f%n", base.calculateTotal());
        System.out.println(checkout.getReceiptBreakdown());
        System.out.printf("FINAL CHARGE TOTAL: $%.2f%n", checkout.calculateTotal());
    }
}
