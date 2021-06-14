package com.kcover.dbdiffer;

import java.util.Map;
import java.util.Objects;

public class NewDbAccount extends Account {
    private String favoriteFlavor;

    public NewDbAccount() {
        super();
    }

    public NewDbAccount(String id, String name, String email, String favoriteFlavor) {
        super(id, name, email);
        this.favoriteFlavor = favoriteFlavor;
    }

    public String getFavoriteFlavor() {
        return favoriteFlavor;
    }

    public void setFavoriteFlavor(String favoriteFlavor) {
        this.favoriteFlavor = favoriteFlavor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NewDbAccount that = (NewDbAccount) o;
        return Objects.equals(getFavoriteFlavor(), that.getFavoriteFlavor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getFavoriteFlavor());
    }

    public static NewDbAccount fromMap(Map<String, Object> map){
        var id = map.get("id");
        var name = map.get("name");
        var email = map.get("email");
        var favoriteFlavor = map.get("favorite_flavor");
        if(!(id instanceof String)){
            throw new RuntimeException("ID was unexpected type of: " + id.getClass());
        }
        if(!(name instanceof String)){
            throw new RuntimeException("Name was unexpected type of: " + name.getClass());
        }
        if(!(email instanceof String)){
            throw new RuntimeException("Email was unexpected type of: " + email.getClass());
        }
        if(!(favoriteFlavor instanceof String)){
            throw new RuntimeException("Favorite flavor was unexpected type of: " + favoriteFlavor.getClass());
        }
        return new NewDbAccount((String) id,(String) name,(String) email,(String) favoriteFlavor);
    }

    public Account toAccount(){
        return new Account(getId(), getName(), getEmail());
    }

    public static NewDbAccount fromAccount(Account account){
        return new NewDbAccount(account.getId(), account.getName(), account.getEmail(), null);
    }

    @Override
    public String toSqlValue(){
        return String.format("('%s', '%s', '%s', '%s')", getId(), getName(), getEmail(), getFavoriteFlavor());
    }
}
