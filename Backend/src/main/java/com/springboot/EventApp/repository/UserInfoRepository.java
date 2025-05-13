package com.springboot.EventApp.repository;

import com.springboot.EventApp.model.entities.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for accessing and managing UserInfo entities in the database.
 * <br>
 * Uses JpaRepository for ease of operations
 *
 * @author Lucas Horn
 */
@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByUsername(String username);
}