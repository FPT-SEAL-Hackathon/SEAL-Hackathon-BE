package com.fpt.swp.sealhackathonbe.core.infrastructureDB;
import jakarta.persistence.*;
import jakarta.persistence.Persistence;

public class JpaUtil {
    private static EntityManagerFactory emf;
    static{
        try{
            emf = Persistence.createEntityManagerFactory("com.fpt.swp.sealhackathonbe");
        }catch (Exception e){
            throw new RuntimeException();
        }
    }
    private JpaUtil(){
    }
    public static EntityManager getEntityManager(){
        return emf.createEntityManager();
    }
    public static void shutdown(){
        if(emf != null){
            emf.close();
        }
    }
}
