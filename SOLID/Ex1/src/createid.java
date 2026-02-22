public class createid {
    public String generate(int currentCount) {
        return IdUtil.nextStudentId(currentCount);
    }
}