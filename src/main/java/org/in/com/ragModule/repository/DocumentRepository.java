package org.in.com.ragModule.repository;

import org.in.com.ragModule.dto.DocumentDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<DocumentDTO, String> {

}