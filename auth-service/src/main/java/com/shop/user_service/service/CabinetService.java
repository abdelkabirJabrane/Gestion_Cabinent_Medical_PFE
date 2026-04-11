package com.shop.user_service.service;

import com.shop.user_service.entity.Cabinet;

import java.util.List;

public interface CabinetService {
    Cabinet createCabinet(Cabinet cabinet);
    Cabinet updateCabinet(Long id, Cabinet cabinet);
    Cabinet getCabinetById(Long id);
    List<Cabinet> getAllCabinets();
    void deleteCabinet(Long id);
    Cabinet updateStatus(Long id, String status);
    Cabinet updatePlan(Long id, String plan, String dateExpiration);
}
