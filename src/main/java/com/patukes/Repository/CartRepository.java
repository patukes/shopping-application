package com.patukes.Repository;

import java.util.List;

import com.patukes.model.Bufcart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional //if one item fails in the purchase, it should return all, say insufficient money, for example
public interface CartRepository extends JpaRepository<Bufcart, Long> {

    List<Bufcart> findByEmail(String email);

    Bufcart findByBufcartIdAndEmail(int bufcartId, String email);

    void deleteByBufcartIdAndEmail(int bufcartId, String email);

    List<Bufcart> findAllByEmail(String email);

    List<Bufcart> findAllByOrderId(int orderId);
}