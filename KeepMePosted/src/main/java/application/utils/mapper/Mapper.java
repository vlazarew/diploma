package application.utils.mapper;

import application.data.model.telegram.AbstractTelegramEntity;

public interface Mapper<E extends AbstractTelegramEntity, D> {

    E toEntity(D dto);

    D toDTO(E entity);

}
