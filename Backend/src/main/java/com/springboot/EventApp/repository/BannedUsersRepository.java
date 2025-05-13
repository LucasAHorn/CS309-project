package com.springboot.EventApp.repository;

import com.springboot.EventApp.model.dto.EnrollmentId;
import com.springboot.EventApp.model.entities.BannedUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This links to the database for Banned users
 *
 * @author Lucas Horn
 */
@Repository
public interface BannedUsersRepository extends JpaRepository<BannedUsers, EnrollmentId> {
}
