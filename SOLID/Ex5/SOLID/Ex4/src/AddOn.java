public enum AddOn {
    MESS(1000.0), LAUNDRY(500.0), GYM(300.0);
    private final double price;
    AddOn(double price) { this.price = price; }
    public double getPrice() { return price; }
}
