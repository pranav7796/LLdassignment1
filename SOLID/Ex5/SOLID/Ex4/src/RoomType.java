public enum RoomType {
    SINGLE(14000.0),
    DOUBLE(15000.0),
    TRIPLE(12000.0),
    DELUXE(16000.0);

    private final double basePrice;
    RoomType(double basePrice) { this.basePrice = basePrice; }
    public double getBasePrice() { return basePrice; }
}
