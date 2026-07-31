package com.nastolka.repository;

import com.nastolka.entity.Role;
import com.nastolka.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleSub(String googleSub);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findTop20ByUsernameContainingIgnoreCase(String query);

    List<User> findTop20ByUsernameContainingIgnoreCaseAndUsernameNotAndRoleNot(
            String query, String excludedUsername, Role role);
}
