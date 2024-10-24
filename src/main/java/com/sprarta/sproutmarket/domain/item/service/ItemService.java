package com.sprarta.sproutmarket.domain.item.service;

import com.sprarta.sproutmarket.domain.areas.service.AdministrativeAreaService;
import com.sprarta.sproutmarket.domain.category.entity.Category;
import com.sprarta.sproutmarket.domain.category.service.CategoryService;
import com.sprarta.sproutmarket.domain.common.entity.Status;
import com.sprarta.sproutmarket.domain.common.enums.ErrorStatus;
import com.sprarta.sproutmarket.domain.common.exception.ApiException;
import com.sprarta.sproutmarket.domain.item.dto.request.FindItemsInMyAreaRequestDto;
import com.sprarta.sproutmarket.domain.item.dto.request.ItemContentsUpdateRequest;
import com.sprarta.sproutmarket.domain.item.dto.request.ItemCreateRequest;
import com.sprarta.sproutmarket.domain.item.dto.response.ItemResponse;
import com.sprarta.sproutmarket.domain.item.dto.response.ItemResponseDto;
import com.sprarta.sproutmarket.domain.item.entity.Item;
import com.sprarta.sproutmarket.domain.item.entity.ItemSaleStatus;
import com.sprarta.sproutmarket.domain.item.repository.ItemRepository;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import com.sprarta.sproutmarket.domain.user.enums.UserRole;
import com.sprarta.sproutmarket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final AdministrativeAreaService admAreaService;


    /**
     * 로그인한 사용자가 중고 물품을 등록하는 로직
     * @param itemCreateRequest 매물 세부 정보를 포함한 요청 객체(제목, 설명, 가격, 카테고리id)
     * @param authUser 매물 수정을 요청한 사용자
     * @return ItemResponse - 등록된 매물의 제목, 가격, 등록한 사용자의 닉네임을 포함한 응답 객체
     */
    @Transactional
    public ItemResponse createItem(ItemCreateRequest itemCreateRequest, CustomUserDetails authUser){
        // response(user.email)를 위해 AuthUser에서 사용자 정보 가져오기
        User user = userRepository.findById(authUser.getId())
            .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        // 카테고리 찾기
        Category findCategory = categoryService.findByIdOrElseThrow(itemCreateRequest.getCategoryId());


        Item item = Item.builder()
            .title(itemCreateRequest.getTitle())
            .description(itemCreateRequest.getDescription())
            .price(itemCreateRequest.getPrice())
            .itemSaleStatus(ItemSaleStatus.WAITING)
            .category(findCategory)
            .seller(user)
            .status(Status.ACTIVE)
            .build();

        itemRepository.save(item);

        return new ItemResponse(
            item.getTitle(),
            item.getPrice(),
            user.getNickname()
        );
    }

    /**
     * 매물의 판매 상태만을 변경하는 로직
     * @param itemId Item's ID
     * @param itemSaleStatus 판매 상태
     * @param authUser 매물 판매 상태 수정을 요청한 사용자
     * @return ItemResponse - 등록된 매물의 제목, 가격, 판매상태, 등록한 사용자의 닉네임을 포함한 응답 객체
     */
    @Transactional
    public ItemResponse updateSaleStatus(Long itemId, String itemSaleStatus, CustomUserDetails authUser) {
        // response(user.email)를 위해 AuthUser에서 사용자 정보 가져오기
        User user = userRepository.findById(authUser.getId())
            .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        // 매물 존재하는지, 해당 유저의 매물이 맞는지 확인
        Item item = findByIdAndSellerIdOrElseThrow(itemId, user.getId());

        ItemSaleStatus newItemSaleStatus = ItemSaleStatus.of(itemSaleStatus);
        item.changeSaleStatus(newItemSaleStatus);

        itemRepository.save(item);

        return new ItemResponse(
            item.getTitle(),
            item.getPrice(),
            item.getItemSaleStatus(),
            user.getNickname()
        );
    }

    /**
     * 매물의 내용(제목, 설명, 가격, 이미지URL)을 수정하는 로직
     * @param itemId Item's ID
     * @param itemContentsUpdateRequest 매물 수정 정보를 포함한 요청 객체(제목, 내용, 가격, 이미지URL)
     * @param authUser 매물 내용 수정을 요청한 사용자
     * @return ItemResponse - 등록된 매물의 제목, 설명, 가격, 등록한 사용자의 닉네임을 포함한 응답 객체
     */
    @Transactional
    public ItemResponse updateContents(Long itemId, ItemContentsUpdateRequest itemContentsUpdateRequest, CustomUserDetails authUser){
        // response(user.email)를 위해 AuthUser에서 사용자 정보 가져오기
        User user = userRepository.findById(authUser.getId())
            .orElseThrow(() ->  new ApiException(ErrorStatus.NOT_FOUND_USER));

        // 매물 존재하는지, 해당 유저의 매물이 맞는지 확인
        Item item = findByIdAndSellerIdOrElseThrow(itemId, user.getId());

        item.changeContents(
            itemContentsUpdateRequest.getTitle(),
            itemContentsUpdateRequest.getDescription(),
            itemContentsUpdateRequest.getPrice(),
            itemContentsUpdateRequest.getImageUrl()
        );

        itemRepository.save(item);

        return new ItemResponse(
            item.getTitle(),
            item.getDescription(),
            item.getPrice(),
            user.getNickname()
        );
    }


    /**
     * 자신이 등록한 매물을 논리적 삭제하는 로직
     * @param itemId Item's ID
     * @param authUser 매물 삭제를 요청한 사용자
     * @return ItemResponse - 삭제된 매물의 제목, 상태, 삭제한 사용자의 닉네임을 포함한 응답 객체
     */
    @Transactional
    public ItemResponse softDeleteItem(Long itemId, CustomUserDetails authUser){
        // response(user.email)를 위해 AuthUser에서 사용자 정보 가져오기
        User user = userRepository.findById(authUser.getId())
            .orElseThrow(() ->  new ApiException(ErrorStatus.NOT_FOUND_USER));

        // 매물 존재하는지, 해당 유저의 매물이 맞는지 확인
        Item item = findByIdAndSellerIdOrElseThrow(itemId, user.getId());

        item.solfDelete(
            Status.DELETED
        );

        itemRepository.save(item);

        return new ItemResponse(
            item.getTitle(),
            item.getStatus(),
            user.getNickname()
        );
    }

    /**
     * 관리자가 신고된 매물을 논리적 삭제하는 로직
     * @param itemId Item's ID
     * @return ItemResponse - 삭제된 매물의 제목, 설명, 상태를 포함한 응답 객체
     */
    @Transactional
    public ItemResponse softDeleteReportedItem(Long itemId, CustomUserDetails authUser){
        User user = userRepository.findById(authUser.getId())
            .orElseThrow(() ->  new ApiException(ErrorStatus.NOT_FOUND_USER));

        if(!authUser.getRole().equals(UserRole.ADMIN)){
            throw new ApiException(ErrorStatus.FORBIDDEN_TOKEN);
        }
        // 매물 존재하는지, 해당 유저의 매물이 맞는지 확인
        Item item = findByIdOrElseThrow(itemId);

        item.solfDelete(
            Status.DELETED
        );

        itemRepository.save(item);

        return new ItemResponse(
            item.getTitle(),
            item.getDescription(),
            item.getStatus()
        );
    }

    /**
     * 로그인 한 사용자가 특정 매물을 상세조회하는 로직
     * @param itemId Item's ID
     * @return ItemResponseDto - Item에 있는 모든 정보값을 포함한 응답 객체
     */
    public ItemResponseDto getItem(Long itemId){
        // 매물 존재하는지, 해당 유저의 매물이 맞는지 확인
        Item item = findByIdOrElseThrow(itemId);

        return new ItemResponseDto(
            item.getId(),
            item.getTitle(),
            item.getDescription(),
            item.getPrice(),
            item.getSeller().getNickname(),
            item.getItemSaleStatus(),
            item.getCategory().getName(),
            item.getStatus()
        );
    }


    /**
     * 현재 인증된 사용자의 모든 매물을 조회하는 로직
     * @param page 페이지 번호(1부터 시작)
     * @param size 페이지당 카드 수
     * @param authUser 현재 인증된 사용자 정보
     * @return Page<ItemResponseDto> - 요청된 페이지에 해당하는 현재 인증된 사용자의 매물 목록을 포함한 페이지 정보
     *          각 매물은 ItemResponseDto 형태로 변환되어 반환됨
     *          매물들의 상세 정보와 페이지 정보를 포함하고 있음
     */
    public Page<ItemResponseDto> getMyItems(int page, int size, CustomUserDetails authUser){
        // AuthUser에서 사용자 정보 가져오기
        User user = userRepository.findById(authUser.getId())
            .orElseThrow(() ->  new ApiException(ErrorStatus.NOT_FOUND_USER));

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Item> items = itemRepository.findBySeller(pageable, user);

        return items.map(item -> new ItemResponseDto(
            item.getId(),
            item.getTitle(),
            item.getDescription(),
            item.getPrice(),
            item.getSeller().getNickname(),
            item.getItemSaleStatus(),
            item.getCategory().getName(),
            item.getStatus()
            )
        );
    }


    /**
     * 특정 카테고리에 모든 매물을 조회
     * @param page 페이지 번호(1부터 시작)
     * @param size 페이지당 카드 수
     * @param categoryId Category's ID
     * @return Page<ItemResponseDto> - 요청된 페이지에 해당하는 특정 카테고리의 매물 목록을 포함한 페이지 정보
     *      *          각 매물은 ItemResponseDto 형태로 변환되어 반환됨
     *      *          매물들의 상세 정보와 페이지 정보를 포함하고 있음
     */
    public Page<ItemResponseDto> getCategoryItems(int page, int size, Long categoryId){
        // 카테고리 존재 확인
        Category findCategory = categoryService.findByIdOrElseThrow(categoryId);

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Item> items = itemRepository.findByCategory(pageable, findCategory);

        return items.map(item -> new ItemResponseDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getPrice(),
                item.getSeller().getNickname(),
                item.getItemSaleStatus(),
                item.getCategory().getName(),
                item.getStatus()
            )
        );
    }

    /**
     * 내 주변에 있는 매물을 조회하는 메서드입니다.
     * 추후 논의를 통해 다른 조회 시에도 해당 기능을 합칠 예정입니다.
     * @param authUser : 현재 인증받은 사용자
     * @param requestDto : 페이지번호, 크기가 들어있는 requestDto, @valid 사용하기 위해 파라미터로 안 받고 DTO 로 받았습니다.
     * @return 페이징해서 찾은 매물들 DTO 로 매핑해서 반환
     */
    public Page<ItemResponseDto> findItemsByMyArea (CustomUserDetails authUser, FindItemsInMyAreaRequestDto requestDto) {
        //User.fromAuthUser 쓰면 User 안에 있는 Address를 못 불러와서 이렇게 꺼냈습니다.
        User currentUser = userRepository.findById(authUser.getId()).orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));
        String myArea = currentUser.getAddress();

        List<String> areaList = admAreaService.findAdmNameListByAdmName(myArea);
        Pageable pageable = PageRequest.of(requestDto.getPage()-1, requestDto.getSize());
        Page<Item> result = itemRepository.findByAreaListAndUserArea(pageable,areaList);

        return result.map(item -> new ItemResponseDto(
                        item.getId(),
                        item.getTitle(),
                        item.getDescription(),
                        item.getPrice(),
                        item.getSeller().getNickname(),
                        item.getItemSaleStatus(),
                        item.getCategory().getName(),
                        item.getStatus()
                )
        );
    }

    /**
     * 주어진 id에 해당하는 Item을 찾고,
     * 존재하지 않을 경우 ItemNotFoundException을 던집니다.
     * @param id Item's ID
     * @return Item 객체
     * @throws ApiException 해당 id의 매물이 존재하지 않을 경우 발생
     */
    public Item findByIdOrElseThrow(Long id){
        return itemRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_ITEM));
    }

    /**
     * 주어진 id에 해당하는 Item을 찾고,
     * 존재하지 않을 경우 ItemNotFoundException을 던집니다.
     * @param itemId Item's ID
     * @param sellerId User's ID
     * @return Item 객체
     * @throws ApiException 해당 id의 사용자 id와 입력받은 sellerId와 동일하지 않을 경우 발생
     */
    public Item findByIdAndSellerIdOrElseThrow(Long itemId, Long sellerId){
        return itemRepository.findByIdAndSellerId(itemId, sellerId)
            .orElseThrow(() -> new ApiException(ErrorStatus.FORBIDDEN_NOT_OWNED_ITEM));
    }

}
