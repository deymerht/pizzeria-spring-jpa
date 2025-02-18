package com.deymer.pizzeria.service;

import com.deymer.pizzeria.persistence.entity.PizzaEntity;
import com.deymer.pizzeria.persistence.repository.PizzaPageSortRepository;
import com.deymer.pizzeria.persistence.repository.PizzaRepository;
import com.deymer.pizzeria.service.dto.UpdatePizzaPriceDto;
import com.deymer.pizzeria.service.exeption.EmailApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PizzaService {
    @Autowired
    private PizzaRepository pizzaRepository;

    @Autowired
    private PizzaPageSortRepository pizzaPageSortRepository;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PizzaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PizzaEntity> getAllWithJdbcTemplate(){
        final String query = "SELECT * FROM pizza";
        return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(PizzaEntity.class));
    }

    public Optional<PizzaEntity> getByIdWithJdbcTemplate(Integer idPizza) {
        final String query = "SELECT * FROM pizza WHERE id_pizza = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(query, new BeanPropertyRowMapper<>(PizzaEntity.class), idPizza));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Page<PizzaEntity> getAll(int page, int amountElements){
        //return pizzaRepository.findAll();
        Pageable pageRequest = PageRequest.of(page, amountElements);
        return pizzaPageSortRepository.findAll(pageRequest);
    }

    public List<PizzaEntity> getAvailable(Boolean available){
        return pizzaRepository.findAllByAvailableOrderByPrice(available);
    }


    public Page<PizzaEntity> getAvailableWithPage(int page, int elements, String sortBy, String sortDirection) {
        System.out.println(this.pizzaRepository.countByVeganTrue());
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageRequest = PageRequest.of(page, elements, sort);
        return this.pizzaPageSortRepository.findByAvailableTrue(pageRequest);
    }

    public PizzaEntity getByName(String name){
        return pizzaRepository.findFirstByAvailableTrueAndNameIgnoreCase(name).orElseThrow(()-> new RuntimeException("The pizza is not exist"));
    }

    public List<PizzaEntity> findPizzasByKeyword(String keyword) {
        return pizzaRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }

    public List<PizzaEntity> getWith(String ingredient) {
        return this.pizzaRepository.findAllByAvailableTrueAndDescriptionContainingIgnoreCase(ingredient);
    }

    public List<PizzaEntity> without(String ingredient) {
        return this.pizzaRepository.findAllByAvailableTrueAndDescriptionNotContainingIgnoreCase(ingredient);
    }

    public List<PizzaEntity> getCheapest(double price) {
        return this.pizzaRepository.findTop3ByAvailableTrueAndPriceLessThanEqualOrderByPriceAsc(price);
    }

    public PizzaEntity get(Integer idPizza) {
        return pizzaRepository.findById(idPizza).orElse(null);
    }

    public PizzaEntity save(PizzaEntity pizza) {
        return pizzaRepository.save(pizza);
    }

    public void delete(Integer idPizza) {
        pizzaRepository.deleteById(idPizza);
    }

    public boolean exists(Integer idPizza){
        return pizzaRepository.existsById((idPizza));
    }

    @Transactional(noRollbackFor = EmailApiException.class)// Cuando ocurra esta exeption no hará roolback
    public void updatePrice(UpdatePizzaPriceDto dto) {
        this.pizzaRepository.updatePrice(dto);
        this.sendEmail();;
    }

    private void sendEmail() {
        throw new EmailApiException();
    }

}
