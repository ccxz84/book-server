package com.ssu.commerce.book.dto.mapper;

import com.ssu.commerce.book.dto.param.UpdateBookStateRequestDto;
import com.ssu.commerce.book.constant.code.BookState;
import com.ssu.commerce.book.grpc.UpdateBookStateType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UpdateBookStateDtoMapper {

    UpdateBookStateDtoMapper INSTANCE = Mappers.getMapper(UpdateBookStateDtoMapper.class);

    @Mapping(source = "id", target = "id", qualifiedByName = "stringToUUID")
    @Mapping(source = "state", target = "state", qualifiedByName = "mapState")
    UpdateBookStateRequestDto map(UpdateBookStateType proto);

    @Named("stringToUUID")
    default UUID stringToUUID(String id) {
        return UUID.fromString(id);
    }

    @Named("mapState")
    default BookState mapState(com.ssu.commerce.book.grpc.BookState state) {
        return BookState.valueOf(state.name());
    }
}