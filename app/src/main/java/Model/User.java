package Model;

import androidx.annotation.NonNull;

public class User {
    private int age;
    private String gender;
    private float weight;
    private float height;
    private float bmiGoal;

    public User() {
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "age=" + age + "," +
                "gender: '" + gender + '\'' + "," +
                "weight=" + weight + "," +
                "height=" + height + "," +
                "bmiGoal=" + bmiGoal +
                '}';
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getBmiGoal() {
        return bmiGoal;
    }

    public void setBmiGoal(float bmiGoal) {
        this.bmiGoal = bmiGoal;
    }
}
