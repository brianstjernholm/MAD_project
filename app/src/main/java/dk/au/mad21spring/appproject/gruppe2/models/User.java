package dk.au.mad21spring.appproject.gruppe2.models;

public class User {
    private String Id;
    private String Username;
    private String imageURL;

    public User(String id, String username, String imageURL) {
        Id = id;
        Username = username;
        this.imageURL = imageURL;
    }

    //firebase need default constructor
    public User() { }

    //getters+setters
    public String getId() { return Id; }

    public void setId(String id) { Id = id; }

    public String getUsername() { return Username; }

    public void setUsername(String username) { Username = username; }

    public String getImageURL() { return imageURL; }

    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
}
