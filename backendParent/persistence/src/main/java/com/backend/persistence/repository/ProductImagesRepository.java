package com.backend.persistence.repository;

import java.util.List;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.core.entity.Tenant;
import com.backend.persistence.entity.Product;
import com.backend.persistence.entity.ProductImages;

/**
 * @author Muhil
 *
 */
@Repository
public interface ProductImagesRepository extends JpaRepository<ProductImages, Long> {
	
	String findAllImagesForProductQuery = "select pi from ProductImages pi where pi.tenant = :tenant and pi.productId = :pId";
	String findAllImagesForProductByIdQuery = "select * from ProductImages where tenantid = :tenant and productid = :pId";
	String findImageByIdQuery  = "select pi from ProductImages pi where pi.tenant = :tenant and pi.pImagesId = :imageId";
	String deleteByIdQuery = "delete from ProductImages pi where pi.tenant = :tenant and pi.pImagesId = :imageId";
	String getProductCountByIdQuery = "select count(*) from ProductImages pi where pi.tenant = :tenant and pi.productId = :pId";
	
	@Query(findAllImagesForProductQuery)
	List<ProductImages> findAllImagesForProduct(@Param("tenant") Tenant realm, @Param("pId") Product product);
	
	@Query(value = findAllImagesForProductByIdQuery, nativeQuery = true)
	List<ProductImages> findAllImagesForProduct(@Param("tenant") String realm, @Param("pId") Long pId);
	
	@Query(findImageByIdQuery)
	ProductImages findImageById(@Param("tenant") Tenant realm, @Param("imageId") Long imageId);
	
	@Modifying
	@Cascade(CascadeType.DELETE)
	@Query(deleteByIdQuery)
	void deleteById(@Param("tenant") Tenant realm, @Param("imageId") Long imageId);
	
	@Query(getProductCountByIdQuery)
	int getProductCount(@Param("tenant") Tenant realm, @Param("pId") Product product);

}
