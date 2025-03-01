# rest-patch

This library aims to aid in supporting patching for requests in Java-based RESTFUL webservices. It is not tied into Spring in anyway, but all examples here are shown using Spring Boot. It aims to support the logical way of handling REST patching in services like [Stripe](https://stripe.com) and [Previsto](https://previsto.com) and hence support JSON and FORM based requests.

## What does it do?

### Merging
First of all it allows you to patch a POJO upon another POJO of same type based on a list of dirty fields. You simply gather a list of dirty fields which you want to be patched from one POJO to another. You use the `EntityMerger` for this purpose as shown below.

__Merge Example__
```Java
public void main() {
  EntityMerger merger = new EntityMerger();
  Pet patch = new Pet();
  patch.setName("Bella");
  path.setKind(Kind.Cat);
  
  Pet original = new Pet();
  original.setName("Barkley");
  original.setKind(Kind.Dog);
  
  List<String> dirtyFields = Collections.singletonList("name");
  merger.mergeEntities(original, patch, dirtyFields);
  
  System.out.println(original.getName()); // Outputs "Bella" (Was patched)
  System.out.println(original.getKind()); // Outputs "Dog" (Was NOT patched)  
}
```

### Resolve dirty fields
Second, it allows you to gather the dirty fields from JSON(via Jackson) and FORM(via Java Map) requests. Checkout the following examples:

__JSON Example (Spring Boot)__
```Java
@RestController
public class PetController {

  private TreeNodePropertyReferenceConverter fieldConverter = new TreeNodePropertyReferenceConverter();
  private EntityMerger<et> merger = new EntityMerger();
    
  @PutMapping("/pets/{id}")
  public Pet update(@RequestBody Pet patch, @PathVariable String id) {
    Pet original = ...;  // Get original from backend
    List<String> fields = fieldConverter.translate(TreeNodeHolder.get());
    Pet merge = this.merger.mergeEntities(original, patch, fields);
    ... // Save merge to backend
  } 
}
```
The above example allows us to send just the fields we want to update, fx. `{"name": "Bessie"}`, without overwriting other fields.

It gives you the same flexibility with FORM input which is handy when it comes to supporting access to the API via cUrl.

__FORM Example (Spring Boot)__
```Java
@RestController
public class PetController {

  private FormPropertyReferenceConverter fieldConverter = new FormPropertyReferenceConverter();
  private EntityMerger<et> merger = new EntityMerger();
    
  @PutMapping("/pets/{id}", consumes = "application/x-www-form-urlencoded")
  public Pet update(@ModelAttribute Pet patch, @PathVariable String id, HttpServletRequest request) {
    Pet original = ...;  // Get original from backend
    List<String> fields = fieldConverter.translate(request.getParameterMap()));
    Pet merge = this.merger.mergeEntities(original, patch, fields);
    ... // Save merge to backend
  } 
}
```
The above example allows us to send just the fields we want to update via cUrl, fx. `curl -X PUT -d name=Bessie http://server/pets/{id}`, without overwriting other fields.