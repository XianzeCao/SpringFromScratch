The first version uses about 300 lines to implement the core features that define spring:
IOC -- inversion of control 
DI -- dependency injection 
MVC 

All functionality will be packed into one class -- DispatcherServlet
Obviously this does follow the design principles in practice 
So in version 2 I will refactor the project into roughly 30 classes and add more features and functionalities(AOP, ORM...) to it

Spring in 3 stages:
1.configuration 


2.initialization:
    a. invoke init() method to load config files
    b. init IOC container
    c. scan the package for relevant classes 
    d. load class instance into IOC container
    e. DI
    f. init handlerMapping, construct mapping between URLs and methods 


3.running

    