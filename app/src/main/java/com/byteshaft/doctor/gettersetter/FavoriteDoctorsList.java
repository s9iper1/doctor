package com.byteshaft.doctor.gettersetter;

/**
 * Created by husnain on 4/15/17.
 */

public class FavoriteDoctorsList {

    public String getDoctorsName() {
        return doctorsName;
    }

    public void setDoctorsName(String doctorsName) {
        this.doctorsName = doctorsName;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public boolean isTimeStatus() {
        return timeStatus;
    }

    public void setTimeStatus(boolean timeStatus) {
        this.timeStatus = timeStatus;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getDoctorImage() {
        return doctorImage;
    }

    public void setDoctorImage(String doctorImage) {
        this.doctorImage = doctorImage;
    }

    public String getDoctorsLocation() {
        return doctorsLocation;
    }

    public void setDoctorsLocation(String doctorsLocation) {
        this.doctorsLocation = doctorsLocation;
    }

    public int getTimeId() {
        return timeId;
    }

    public void setTimeId(int timeId) {
        this.timeId = timeId;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    private String doctorsName;
    private String speciality;
    private int stars;
    private int timeId;
    private int slotId;
    private boolean timeStatus;
    private String timeSlot;
    private String doctorImage;
    private String doctorsLocation;

}
