package com.lukas.imguploader.repository;


import com.lukas.imguploader.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findUserByUsername(String username);

  Optional<User> findUserByEmail(String email);

  Optional<User> findUserById(Long id);

}
