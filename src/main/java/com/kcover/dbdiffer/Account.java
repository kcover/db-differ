package com.kcover.dbdiffer;

import java.util.Map;
import java.util.Objects;

/** A POJO for interacting with accounts. */
public class Account {
  private String id;
  private String name;
  private String email;

  public Account() {}

  public Account(String id, String name, String email) {
    this.id = id;
    this.name = name;
    this.email = email;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public static Account fromMap(Map<String, Object> map) {
    var id = map.get("id");
    var name = map.get("name");
    var email = map.get("email");
    if (!(id instanceof String)) {
      throw new RuntimeException("ID was unexpected type of: " + id.getClass());
    }
    if (!(name instanceof String)) {
      throw new RuntimeException("Name was unexpected type of: " + name.getClass());
    }
    if (!(email instanceof String)) {
      throw new RuntimeException("Email was unexpected type of: " + email.getClass());
    }
    return new Account((String) id, (String) name, (String) email);
  }

  public String toSqlValue() {
    return String.format("('%s', '%s', '%s')", getId(), getName(), getEmail());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Account account = (Account) o;
    return id.equals(account.id)
        && Objects.equals(name, account.name)
        && Objects.equals(email, account.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, email);
  }

  @Override
  public String toString() {
    return "Account{" + "id=" + id + ", name='" + name + '\'' + ", email='" + email + '\'' + '}';
  }
}
