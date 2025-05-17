# Category Service for Property Management

This microservice manages property categories for the e-commerce real estate application, with specific integration for the property service.

## Features

- CRUD operations for property categories
- Hierarchical categories (parent-child relationships)
- Property type-specific categories
- Featured categories for homepage display
- Active/inactive category status
- Property count tracking per category
- Integration with Eureka Service Registry
- Centralized configuration with Spring Cloud Config

## Property Service Integration

This service is specifically designed to work with the property service:

1. **Property Type Categories**: Categories can be associated with specific property types (House, Apartment, etc.)
2. **Property Count Tracking**: Keeps track of how many properties are in each category
3. **Featured Categories**: Identifies categories to highlight on the property listings page
4. **Hierarchical Navigation**: Supports parent-child category relationships for property filtering

## API Endpoints

### Standard Endpoints

```
POST    /api/categories                  - Create category
PUT     /api/categories/{id}             - Update category
DELETE  /api/categories/{id}             - Delete category (soft delete)
GET     /api/categories/{id}             - Get category by ID
GET     /api/categories                  - Get all categories
GET     /api/categories/active           - Get all active categories
GET     /api/categories/parent           - Get all parent categories
GET     /api/categories/parent/{id}/children - Get child categories
GET     /api/categories/check?name=XXX   - Check if category exists
```

### Property Service Integration Endpoints

```
GET     /api/categories/property-type/{type}   - Get categories for property type
GET     /api/categories/property-types?types=  - Get categories for multiple property types
GET     /api/categories/featured              - Get featured categories for homepage
GET     /api/categories/{id}/with-children    - Get category with its children
PUT     /api/categories/{id}/property-count   - Update property count for a category
```

## Database Schema

The service uses a PostgreSQL database with the following schema:

- **categories** table:
  - id (Primary Key)
  - name (Unique)
  - description
  - image_url
  - parent_id (Foreign Key to categories.id)
  - featured_category
  - is_active

- **category_property_types** table:
  - category_id (Foreign Key to categories.id)
  - property_type

## Using from Property Service

To use this service from the property service:

1. Add categories to properties
2. Filter properties by category
3. Display featured categories
4. Update property counts when properties are added/removed
5. Use hierarchical categories for navigation

## Integration Example

```java
// In PropertyService.java
@FeignClient(name = "category-service")
public interface CategoryClient {
    @GetMapping("/api/categories/property-type/{type}")
    List<PropertyCategoryDTO> getCategoriesForPropertyType(@PathVariable String type);
    
    @PutMapping("/api/categories/{id}/property-count")
    void updatePropertyCount(@PathVariable Long id, @RequestParam Long count);
}
``` 