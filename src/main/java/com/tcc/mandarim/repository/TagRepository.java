package com.tcc.mandarim.repository;

import com.tcc.mandarim.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {

    Optional<Tag> findByNome(String nome);

    boolean existsByNome(String nome);
}
