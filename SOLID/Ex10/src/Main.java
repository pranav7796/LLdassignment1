public class Main {
    public static void main(String[] args) {
        System.out.println("=== Transport Booking ===");
        TripRequest req = new TripRequest("23BCS1010", new GeoPoint(12.97, 77.59), new GeoPoint(12.93, 77.62));

        DistanceService distanceService = new DistanceCalculator();
        DriverAllocationService driverService = new DriverAllocator();
        PaymentService paymentService = new PaymentGateway();

        TransportBookingService svc = new TransportBookingService(distanceService, driverService, paymentService);
        svc.book(req);
    }
}
