package com.parking.web;
import com.parking.entity.*;
import com.parking.repo.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AuthController {
  public record RegisterRequest(@NotBlank String username, @NotBlank @Size(min = 8) String password,
      @Email @NotBlank String email, String fullName, String phone, String address,
      LocalDate birthDate, String gender, String photoUrl) {}
  public record CreateUserRequest(@NotBlank String username, @Email @NotBlank String email,
      @NotBlank @Size(min = 8) String password, @NotNull Role role) {}

  private final UserRepository users;
  private final PasswordEncoder encoder;
  public AuthController(UserRepository u, PasswordEncoder e) { users = u; encoder = e; }

  private User build(String username, String email, String password, Role role) {
    if (users.existsByUsername(username) || users.existsByEmail(email))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuario o correo ya existe");
    User u = new User();
    u.setUsername(username); u.setEmail(email);
    u.setPasswordHash(encoder.encode(password));
    u.setRole(role); u.setActive(true);
    return u;
  }

  /** RF-01: autoregistro. Rol OPERATOR e inactivo hasta que un Administrador lo active. */
  @PostMapping("/auth/register") @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> register(@RequestBody @Valid RegisterRequest r) {
    User u = build(r.username(), r.email(), r.password(), Role.OPERATOR);
    u.setFullName(r.fullName()); u.setPhone(r.phone()); u.setAddress(r.address());
    u.setBirthDate(r.birthDate()); u.setGender(r.gender()); u.setPhotoUrl(r.photoUrl());
    u.setActive(false);
    users.save(u);
    return Map.of("id", u.getId(), "username", u.getUsername(), "active", u.isActive());
  }

  /** RF-02: el frontend envia HTTP Basic; este endpoint valida credenciales y devuelve el perfil. */
  @GetMapping("/auth/me")
  public Map<String, Object> me(Principal p) {
    User u = users.findByUsername(p.getName()).orElseThrow();
    return Map.of("id", u.getId(), "username", u.getUsername(), "role", u.getRole(),
        "fullName", u.getFullName() == null ? "" : u.getFullName());
  }

  /** RF-05 */
  @PostMapping("/users") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> create(@RequestBody @Valid CreateUserRequest r) {
    User u = users.save(build(r.username(), r.email(), r.password(), r.role()));
    return Map.of("id", u.getId(), "username", u.getUsername(), "role", u.getRole());
  }

  /** RF-06 */
  @PatchMapping("/users/{id}/active") @PreAuthorize("hasRole('ADMIN')")
  public Map<String, Object> setActive(@PathVariable Integer id, @RequestParam boolean value) {
    User u = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    u.setActive(value); users.save(u);
    return Map.of("id", id, "active", value);
  }
}
