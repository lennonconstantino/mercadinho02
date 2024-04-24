package br.com.lennon.mercadinho02.repository;


import br.com.lennon.mercadinho02.model.ProductEventKey;
import br.com.lennon.mercadinho02.model.ProductEventLog;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

// serve para localizar a classe durante a inicializacao
@EnableScan
public interface ProductEventLogRepository extends CrudRepository<ProductEventLog, ProductEventKey> {
    // o dynamo varre todos os itens da tabela, se vc não usar a query desse jeito
    // usar chave composta para fazer consulta, assim evita fazer um fullscan na tabela
    List<ProductEventLog> findAllByPk(String code);

    List<ProductEventLog> findAllByPkAndSkStartsWith(String code, String eventType);
}
