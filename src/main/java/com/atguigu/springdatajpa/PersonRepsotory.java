package com.atguigu.springdatajpa;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 1.Repository是一个空接口，即是一个标记接口。
 * 2.若我们定义的接口继承了Repository，则该接口会被IOC容器识别为一个Repository Bean纳入到IOC容器中，进而可以在该接口中定义满足一定规范的方法。
 * 3.实际上，也可以通过@RepositoryDefinition注解来替代继承Repository接口。
 * <p>
 * 在Repository子接口中声明方法：
 * 1.不是随便声明的，而需要符合一定的规范；
 * 2.查询方法以find/read/get开头；
 * 3.涉及条件查询时，条件的属性用条件关键字连接；
 * 4.要注意的是：条件属性以首字母大写；
 * 5.支持属性的级联查询：1).若当前类有符合条件的属性，则优先使用，而不使用级联属性；2).若需要使用级联属性，则属性之间使用“_”进行连接。
 */
//@RepositoryDefinition(domainClass=Person.class,idClass=Integer.class)
public interface PersonRepsotory extends JpaRepository<Person, Integer>, JpaSpecificationExecutor<Person>, PersonDao {

    /**
     * 根据lastName来获取对应的Person
     */
    Person getByLastName(String lastName);

    /**
     * WHERE lastName LIKE ?% AND id < ?
     */
    List<Person> getByLastNameStartingWithAndIdLessThan(String lastName, Integer id);

    /**
     * WHERE lastName LIKE %? AND id < ?
     */
    List<Person> getByLastNameEndingWithAndIdLessThan(String lastName, Integer id);

    /**
     * WHERE email IN (?, ?, ?) OR birth < ?
     */
    List<Person> getByEmailInOrBirthLessThan(List<String> emails, Date birth);

    /**
     * WHERE a.id > ?
     */
    List<Person> getByAddress_IdGreaterThan(Integer id);

    /**
     * 查询id值最大的那个Person
     * 使用@Query注解可以自定义JPQL语句以实现更灵活的查询
     */
    @Query("SELECT p FROM Person p WHERE p.id = (SELECT max(p2.id) FROM Person p2)")
    Person getMaxIdPerson();

    /**
     * 为@Query注解传递参数的方式1：使用占位符
     */
    @Query("SELECT p FROM Person p WHERE p.lastName = ?1 AND p.email = ?2")
    List<Person> testQueryAnnotationParams1(String lastName, String email);

    /**
     * 为@Query注解传递参数的方式2：命名参数的方式
     */
    @Query("SELECT p FROM Person p WHERE p.lastName = :lastName AND p.email = :email")
    List<Person> testQueryAnnotationParams2(@Param("email") String email, @Param("lastName") String lastName);

    /**
     * SpringData允许在占位符上添加%%
     */
    @Query("SELECT p FROM Person p WHERE p.lastName LIKE %?1% OR p.email LIKE %?2%")
    List<Person> testQueryAnnotationLikeParam(String lastName, String email);

    /**
     * SpringData允许在占位符上添加%%
     */
    @Query("SELECT p FROM Person p WHERE p.lastName LIKE %:lastName% OR p.email LIKE %:email%")
    List<Person> testQueryAnnotationLikeParam2(@Param("email") String email, @Param("lastName") String lastName);

    /**
     * 设置nativeQuery=true即可以使用原生的SQL查询
     */
    @Query(value = "SELECT count(id) FROM jpa_persons", nativeQuery = true)
    long getTotalCount();

    /**
     * 可以通过自定义的JPQL完成UPDATE和DELETE操作，注意：JPQL不支持使用INSERT。
     * 在@Query注解中编写JPQL语句，但必须使用@Modifying进行修饰，以通知SpringData，这是一个UPDATE或DELETE操作。
     * UPDATE或DELETE操作需要使用事务，此时需要定义Service层，在Service层的方法上添加事务操作。
     * 默认情况下，SpringData的每个方法上有事务，但都是一个只读事务，他们不能完成修改操作。
     */
    @Modifying
    @Query("UPDATE Person p SET p.email = :email WHERE id = :id")
    void updatePersonEmail(@Param("id") Integer id, @Param("email") String email);

}
