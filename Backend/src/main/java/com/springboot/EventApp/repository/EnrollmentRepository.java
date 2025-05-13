package com.springboot.EventApp.repository;

import com.springboot.EventApp.model.dto.EnrollmentId;
import com.springboot.EventApp.model.entities.Enrollment;
import com.springboot.EventApp.model.entities.EventGroup;
import com.springboot.EventApp.model.entities.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * This links to the Enrollment repository (groups that users are in)
 *
 * @author Lucas Horn
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, EnrollmentId> {
    List<Enrollment> findByUser(UserInfo user);
    List<Enrollment> findByGroup(EventGroup group);

    void deleteAllByGroup(EventGroup group);
}