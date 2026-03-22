public class ClassroomController {
    private final DeviceRegistry reg;

    public ClassroomController(DeviceRegistry reg) { this.reg = reg; }

    public void startClass() {
        for (PowerSwitch d : reg.getAll(PowerSwitch.class)) {
            d.powerOn();
        }

        reg.getOnly(InputSource.class).connectInput("HDMI-1");
        reg.getOnly(BrightnessControl.class).setBrightness(60);
        reg.getOnly(TemperatureControl.class).setTemperatureC(24);

        int present = reg.getOnly(AttendanceReader.class).scanAttendance();
        System.out.println("Attendance scanned: present=" + present);
    }

    public void endClass() {
        System.out.println("Shutdown sequence:");
        for (PowerSwitch d : reg.getAll(PowerSwitch.class)) {
            d.powerOff();
        }
    }
}
