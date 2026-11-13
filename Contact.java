import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Contact implements Serializable {

    private static final long serialVersionUID = 1L;

    // Basic contact details
    private String id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String alternatePhone;
    private String category;

    // Additional contact details
    private String notes;
    private String dob;
    private String anniversary;
    private String company;
    private String jobTitle;
    private String nickname;
    private String website;
    private String relationship;
    private String tags;
    private String photoPath;
    private String alternateAddress;
    private String department;
    private String description;

    // Contact status
    private boolean favorite;
    private boolean blocked;
    private boolean emergencyContact;
    private boolean privateContact;

    // Multiple contact information
    private ArrayList<String> additionalPhones;
    private ArrayList<String> additionalEmails;
    private ArrayList<String> socialMediaProfiles;

    // Contact history
    private ArrayList<String> interactionHistory;

    // Dates and statistics
    private Date createdDate;
    private Date updatedDate;
    private Date lastContacted;
    private int viewCount;

    // Main constructor used by Main.java
    public Contact(
            String id,
            String name,
            String phone,
            String email,
            String address,
            String alternatePhone,
            String category,
            String notes,
            String dob,
            String anniversary,
            String company,
            boolean favorite,
            String relationship,
            String tags,
            String photoPath) {

        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.alternatePhone = alternatePhone;
        this.category = category;
        this.notes = notes;
        this.dob = dob;
        this.anniversary = anniversary;
        this.company = company;
        this.favorite = favorite;
        this.relationship = relationship;
        this.tags = tags;
        this.photoPath = photoPath;

        initializeLists();
        this.createdDate = new Date();
        this.updatedDate = new Date();
        this.viewCount = 0;
    }

    // Constructor used by CSV import in Main.java
    public Contact(
            String id,
            String name,
            String phone,
            String email,
            String address,
            String alternatePhone,
            String category,
            String notes,
            String dob,
            String anniversary,
            String company,
            boolean favorite) {

        this(id, name, phone, email, address, alternatePhone,
                category, notes, dob, anniversary, company,
                favorite, "", "", "");
    }

    // Initialize lists
    private void initializeLists() {
        additionalPhones = new ArrayList<>();
        additionalEmails = new ArrayList<>();
        socialMediaProfiles = new ArrayList<>();
        interactionHistory = new ArrayList<>();
    }

    // ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        updateDate();
    }

    // Basic details
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        updateDate();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        updateDate();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        updateDate();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        updateDate();
    }

    public String getAlternatePhone() {
        return alternatePhone;
    }

    public void setAlternatePhone(String alternatePhone) {
        this.alternatePhone = alternatePhone;
        updateDate();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        updateDate();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
        updateDate();
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
        updateDate();
    }

    public String getAnniversary() {
        return anniversary;
    }

    public void setAnniversary(String anniversary) {
        this.anniversary = anniversary;
        updateDate();
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
        updateDate();
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        updateDate();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        updateDate();
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
        updateDate();
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
        updateDate();
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
        updateDate();
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
        updateDate();
    }

    public String getAlternateAddress() {
        return alternateAddress;
    }

    public void setAlternateAddress(String alternateAddress) {
        this.alternateAddress = alternateAddress;
        updateDate();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
        updateDate();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        updateDate();
    }

    // Favorite
    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
        updateDate();
    }

    // Block status
    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
        updateDate();
    }

    // Emergency contact
    public boolean isEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(boolean emergencyContact) {
        this.emergencyContact = emergencyContact;
        updateDate();
    }

    // Private contact
    public boolean isPrivateContact() {
        return privateContact;
    }

    public void setPrivateContact(boolean privateContact) {
        this.privateContact = privateContact;
        updateDate();
    }

    // Multiple phone numbers
    public void addPhoneNumber(String number) {
        if (number != null && !number.trim().isEmpty()
                && !additionalPhones.contains(number)) {
            additionalPhones.add(number);
            updateDate();
        }
    }

    public ArrayList<String> getAdditionalPhones() {
        return additionalPhones;
    }

    public void setAdditionalPhones(ArrayList<String> phones) {
        additionalPhones = phones == null
                ? new ArrayList<String>() : new ArrayList<>(phones);
        updateDate();
    }

    // Multiple email addresses
    public void addEmail(String email) {
        if (email != null && !email.trim().isEmpty()
                && !additionalEmails.contains(email)) {
            additionalEmails.add(email);
            updateDate();
        }
    }

    public ArrayList<String> getAdditionalEmails() {
        return additionalEmails;
    }

    public void setAdditionalEmails(ArrayList<String> emails) {
        additionalEmails = emails == null
                ? new ArrayList<String>() : new ArrayList<>(emails);
        updateDate();
    }

    // Social media profiles
    public void addSocialMediaProfile(String profile) {
        if (profile != null && !profile.trim().isEmpty()) {
            socialMediaProfiles.add(profile);
            updateDate();
        }
    }

    public ArrayList<String> getSocialMediaProfiles() {
        return socialMediaProfiles;
    }

    // Interaction history
    public void addHistory(String history) {
        if (history != null && !history.trim().isEmpty()) {
            interactionHistory.add(history);
            updateDate();
        }
    }

    public ArrayList<String> getInteractionHistory() {
        return interactionHistory;
    }

    public void setInteractionHistory(ArrayList<String> history) {
        interactionHistory = history == null
                ? new ArrayList<String>() : new ArrayList<>(history);
    }

    // Dates
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Date getLastContacted() {
        return lastContacted;
    }

    public void setLastContacted(Date lastContacted) {
        this.lastContacted = lastContacted;
        updateDate();
    }

    // View count
    public int getViewCount() {
        return viewCount;
    }

    public void markAsViewed() {
        viewCount++;
    }

    // Update modification date
    private void updateDate() {
        updatedDate = new Date();
    }

    // Generate unique contact ID
    public static String generateContactID() {
        return UUID.randomUUID().toString();
    }
    // Restore list fields safely when loading saved contacts
private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {

    in.defaultReadObject();

    if (additionalPhones == null)
        additionalPhones = new ArrayList<>();

    if (additionalEmails == null)
        additionalEmails = new ArrayList<>();

    if (socialMediaProfiles == null)
        socialMediaProfiles = new ArrayList<>();

    if (interactionHistory == null)
        interactionHistory = new ArrayList<>();
}
public void setSocialMediaProfiles(ArrayList<String> profiles) {
    this.socialMediaProfiles = (profiles == null)
            ? new ArrayList<>()
            : new ArrayList<>(profiles);
}
public void mergeListsFrom(Contact other) {
    if (other == null) return;

    if (other.getAdditionalPhones() != null) {
        for (String phone : other.getAdditionalPhones()) {
            if (!additionalPhones.contains(phone)) {
                additionalPhones.add(phone);
            }
        }
    }

    if (other.getAdditionalEmails() != null) {
        for (String email : other.getAdditionalEmails()) {
            if (!additionalEmails.contains(email)) {
                additionalEmails.add(email);
            }
        }
    }

    if (other.getSocialMediaProfiles() != null) {
        for (String profile : other.getSocialMediaProfiles()) {
            if (!socialMediaProfiles.contains(profile)) {
                socialMediaProfiles.add(profile);
            }
        }
    }

    if (other.getInteractionHistory() != null) {
        for (String history : other.getInteractionHistory()) {
            if (!interactionHistory.contains(history)) {
                interactionHistory.add(history);
            }
        }
    }
}

    // Display contact information
    @Override
    public String toString() {
        return "Contact{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", category='" + category + '\'' +
                ", favorite=" + favorite +
                '}';
    }
}