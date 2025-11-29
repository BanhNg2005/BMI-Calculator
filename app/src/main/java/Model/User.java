package Model;

import androidx.annotation.NonNull;
import java.util.HashMap;
import java.util.Map;

// Main User class
public class User {
    private Profile profile;
    private PersonalInfo personalInfo;
    private Map<String, Measurement> measurements;
    private Goals goals;
    private Preferences preferences;

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String email, String username) {
        this.profile = new Profile(email, username);
        this.personalInfo = new PersonalInfo();
        this.measurements = new HashMap<>();
        this.goals = new Goals();
        this.preferences = new Preferences();
    }

    // Getters and Setters
    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public PersonalInfo getPersonalInfo() {
        return personalInfo;
    }

    public void setPersonalInfo(PersonalInfo personalInfo) {
        this.personalInfo = personalInfo;
    }

    public Map<String, Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(Map<String, Measurement> measurements) {
        this.measurements = measurements;
    }

    public Goals getGoals() {
        return goals;
    }

    public void setGoals(Goals goals) {
        this.goals = goals;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "profile=" + profile +
                ", personalInfo=" + personalInfo +
                ", measurements=" + measurements +
                ", goals=" + goals +
                ", preferences=" + preferences +
                '}';
    }

    // Profile information - NOW PUBLIC
    public static class Profile {
        private String email;
        private String username;
        private long createdAt;
        private long lastLogin;

        public Profile() {}

        public Profile(String email, String username) {
            this.email = email;
            this.username = username;
            this.createdAt = System.currentTimeMillis();
            this.lastLogin = System.currentTimeMillis();
        }

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
        public long getLastLogin() { return lastLogin; }
        public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }

        @NonNull
        @Override
        public String toString() {
            return "Profile{" +
                    "email='" + email + '\'' +
                    ", username='" + username + '\'' +
                    ", createdAt=" + createdAt +
                    ", lastLogin=" + lastLogin +
                    '}';
        }
    }

    // Personal information - NOW PUBLIC
    public static class PersonalInfo {
        private int age;
        private String gender;
        private float height;
        private float currentWeight;
        private float goalWeight;
        private float goalBmi;

        public PersonalInfo() {}

        // Getters and Setters
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public float getHeight() { return height; }
        public void setHeight(float height) { this.height = height; }
        public float getCurrentWeight() { return currentWeight; }
        public void setCurrentWeight(float currentWeight) { this.currentWeight = currentWeight; }
        public float getGoalWeight() { return goalWeight; }
        public void setGoalWeight(float goalWeight) { this.goalWeight = goalWeight; }
        public float getGoalBmi() { return goalBmi; }
        public void setGoalBmi(float goalBmi) { this.goalBmi = goalBmi; }

        @NonNull
        @Override
        public String toString() {
            return "PersonalInfo{" +
                    "age=" + age +
                    ", gender='" + gender + '\'' +
                    ", height=" + height +
                    ", currentWeight=" + currentWeight +
                    ", goalWeight=" + goalWeight +
                    ", goalBmi=" + goalBmi +
                    '}';
        }
    }

    // Individual measurement/record - NOW PUBLIC
    public static class Measurement {
        private long timestamp;
        private float weight;
        private float height;
        private float bmi;
        private String category;
        private String note;

        public Measurement() {}

        public Measurement(float weight, float height, float bmi, String category) {
            this.timestamp = System.currentTimeMillis();
            this.weight = weight;
            this.height = height;
            this.bmi = bmi;
            this.category = category;
            this.note = "";
        }

        // Getters and Setters
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public float getWeight() { return weight; }
        public void setWeight(float weight) { this.weight = weight; }
        public float getHeight() { return height; }
        public void setHeight(float height) { this.height = height; }
        public float getBmi() { return bmi; }
        public void setBmi(float bmi) { this.bmi = bmi; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }

        @NonNull
        @Override
        public String toString() {
            return "Measurement{" +
                    "timestamp=" + timestamp +
                    ", weight=" + weight +
                    ", height=" + height +
                    ", bmi=" + bmi +
                    ", category='" + category + '\'' +
                    ", note='" + note + '\'' +
                    '}';
        }
    }

    // Goals management - NOW PUBLIC
    public static class Goals {
        private Goal currentGoal;
        private Map<String, Goal> completedGoals;

        public Goals() {
            this.completedGoals = new HashMap<>();
        }

        // Getters and Setters
        public Goal getCurrentGoal() { return currentGoal; }
        public void setCurrentGoal(Goal currentGoal) { this.currentGoal = currentGoal; }
        public Map<String, Goal> getCompletedGoals() { return completedGoals; }
        public void setCompletedGoals(Map<String, Goal> completedGoals) { this.completedGoals = completedGoals; }

        @NonNull
        @Override
        public String toString() {
            return "Goals{" +
                    "currentGoal=" + currentGoal +
                    ", completedGoals=" + completedGoals +
                    '}';
        }
    }

    // Individual goal - NOW PUBLIC
    public static class Goal {
        private float targetWeight;
        private float targetBmi;
        private float startWeight;
        private long startDate;
        private long targetDate;
        private Long completedDate;
        private String status; // active, completed, abandoned
        private float progress;

        public Goal() {}

        public Goal(float targetWeight, float targetBmi, float startWeight, long targetDate) {
            this.targetWeight = targetWeight;
            this.targetBmi = targetBmi;
            this.startWeight = startWeight;
            this.startDate = System.currentTimeMillis();
            this.targetDate = targetDate;
            this.status = "active";
            this.progress = 0f;
        }

        // Getters and Setters
        public float getTargetWeight() { return targetWeight; }
        public void setTargetWeight(float targetWeight) { this.targetWeight = targetWeight; }
        public float getTargetBmi() { return targetBmi; }
        public void setTargetBmi(float targetBmi) { this.targetBmi = targetBmi; }
        public float getStartWeight() { return startWeight; }
        public void setStartWeight(float startWeight) { this.startWeight = startWeight; }
        public long getStartDate() { return startDate; }
        public void setStartDate(long startDate) { this.startDate = startDate; }
        public long getTargetDate() { return targetDate; }
        public void setTargetDate(long targetDate) { this.targetDate = targetDate; }
        public Long getCompletedDate() { return completedDate; }
        public void setCompletedDate(Long completedDate) { this.completedDate = completedDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public float getProgress() { return progress; }
        public void setProgress(float progress) { this.progress = progress; }

        @NonNull
        @Override
        public String toString() {
            return "Goal{" +
                    "targetWeight=" + targetWeight +
                    ", targetBmi=" + targetBmi +
                    ", startWeight=" + startWeight +
                    ", startDate=" + startDate +
                    ", targetDate=" + targetDate +
                    ", completedDate=" + completedDate +
                    ", status='" + status + '\'' +
                    ", progress=" + progress +
                    '}';
        }
    }

    // User preferences - NOW PUBLIC
    public static class Preferences {
        private String unitSystem; // metric or imperial
        private boolean notifications;
        private String reminderTime;
        private String theme; // light or dark

        public Preferences() {
            this.unitSystem = "metric";
            this.notifications = true;
            this.reminderTime = "09:00";
            this.theme = "light";
        }

        // Getters and Setters
        public String getUnitSystem() { return unitSystem; }
        public void setUnitSystem(String unitSystem) { this.unitSystem = unitSystem; }
        public boolean isNotifications() { return notifications; }
        public void setNotifications(boolean notifications) { this.notifications = notifications; }
        public String getReminderTime() { return reminderTime; }
        public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }

        @NonNull
        @Override
        public String toString() {
            return "Preferences{" +
                    "unitSystem='" + unitSystem + '\'' +
                    ", notifications=" + notifications +
                    ", reminderTime='" + reminderTime + '\'' +
                    ", theme='" + theme + '\'' +
                    '}';
        }
    }
}