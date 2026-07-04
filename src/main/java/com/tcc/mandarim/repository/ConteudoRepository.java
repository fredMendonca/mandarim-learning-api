package com.tcc.mandarim.repository;

import com.tcc.mandarim.entity.Conteudo;
import com.tcc.mandarim.entity.enums.TipoConteudo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConteudoRepository extends JpaRepository<Conteudo, Long> {

    List<Conteudo> findByTipo(TipoConteudo tipo);

    Page<Conteudo> findByTipo(TipoConteudo tipo, Pageable pageable);

    List<Conteudo> findByNivelHsk(Integer nivelHsk);

    Page<Conteudo> findByNivelHsk(Integer nivelHsk, Pageable pageable);

    Page<Conteudo> findByTipoAndNivelHsk(TipoConteudo tipo, Integer nivelHsk, Pageable pageable);

    @Query("SELECT c FROM Conteudo c JOIN c.temas t WHERE t.id = :temaId")
    List<Conteudo> findByTemaId(@Param("temaId") Integer temaId);

    @Query("SELECT c FROM Conteudo c JOIN c.tags t WHERE t.id = :tagId")
    List<Conteudo> findByTagId(@Param("tagId") Integer tagId);

    List<Conteudo> findByNivelHskAndTipo(Integer nivelHsk, TipoConteudo tipo);
}
