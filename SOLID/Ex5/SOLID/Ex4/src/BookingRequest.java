import java.util.*;

public class BookingRequest {
    public final RoomType roomType;
    public final List<AddOn> addOns;

    public BookingRequest(RoomType roomType, List<AddOn> addOns) {
        this.roomType = roomType;
        this.addOns = addOns;
    }
}
