package com.clearinghouse.configuration;

import com.clearinghouse.dao.ApplicationSettingDAO;
import com.clearinghouse.dto.*;
import com.clearinghouse.entity.*;
import com.clearinghouse.service.GeoJSONService;
import com.clearinghouse.tds.generated.model.AddressType;
import com.clearinghouse.tds.generated.model.GeographicLocation;
import com.clearinghouse.tds.generated.model.TripRequestResponseType;
import com.clearinghouse.tds.generated.model.TripRequestType;
import jakarta.annotation.PostConstruct;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
@Slf4j
@ComponentScan(basePackages = "com.clearinghouse")
public class SpringAppConfiguration implements WebMvcConfigurer {

    @Autowired
    ApplicationSettingDAO applicationSettingDAO;


    @Autowired
    private GeoJSONService geoJSONService;


    @Value("${hostedSiteUrl}")
    private String hostedSiteUrl;

    @Value("${timezone.abstractEntity}")
    private String entityTimezone;

    @Value("${timezone.syncAPI}")
    private String syncTimezone;


    @Value("${email.host}")
    private String emailHost;

    @Value("${email.port}")
    private String emailPort;

    @Value("${email.username}")
    private String emailUserName;

    @Value("${email.password}")
    private String emailPassword;

    @Value("${email.from}")
    private String emailFrom;

    @Value("${email.to}")
    private String emailTo;

    @Value("${email.subject}")
    private String emailSubject;

    @Value("${email.transport.protocol}")
    private String emailTransportProtocol;

    @Value("${email.smtp.auth}")
    private String emailSmtpAuth;

    @Value("${email.smtp.starttls.enable}")
    private String emailSmtpStarttlsEnable;

    @Value("${email.debug}")
    private String emailDebug;

    @Value("${message.source.basename}")
    private String emailSourceBasename;


    private String filepath;


    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {

        var system = """
                You are an AI powered assistant for the Trip Exchange platform. Your role is to assist users with their queries related to trip management, ticketing, and provider interactions. 
                The trip exchange and coordination between transportation service providers, enabling efficient resource sharing and improved service delivery.
                You should provide clear, concise and helpful responses based on the information available in the Trip Exchange system.
                """;

        return builder
                .defaultSystem(system)
                .build();
    }

    @Bean
    QuestionAnswerAdvisor questionAndAnswerAdvisor(VectorStore vectorStore) {
//        return QuestionAnswerAdvisor.builder(vectorStore)
//                .searchRequest(SearchRequest.builder().similarityThreshold(0.9d).topK(10).build())
//                .build();

        return new QuestionAnswerAdvisor(vectorStore);
    }

    @Bean
    //@Primary
    public String timezoneBean() {
        return entityTimezone;
    }


    @Bean
    public String emailFrom() {
        return emailFrom;
    }

    @Bean
    public String hostedSiteUrl() {
        return hostedSiteUrl;
    }


    @Bean
    public String filePath() {
        if (filepath == null || filepath.isEmpty()) {
            filepath = "";
        }
        return filepath;
    }

    @PostConstruct
    public void initStaticTimezone() {

        AbstractEntity.setTimezone("UTC");
        // Set JVM default timezone to syncAPI timezone to prevent timezone conversions
        // This ensures all Date/Time operations use America/Denver as the reference timezone
        //TimeZone.setDefault(TimeZone.getTimeZone(syncTimezone));
        //log.info("Set JVM default timezone to: {}", syncTimezone);
    }


    @Bean
    public PasswordRuleBean passwordRuleBean() {
        return new PasswordRuleBean();
    }


    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

//        config.setAllowedOrigins(Arrays.asList(
//                "http://52.45.196.193",
//                "52.45.196.193",
//                "http://52.45.196.193:8082",
//                "http://tripexchange.com",
//                "tripexchange.com",
//                "52.45.196.193:8082",
//                "125.99.44.122:9079",
//                "http://125.99.44.122:9079",
//                "http://localhost:4200",
//                "https://localhost:4200",
//                "https://trip-exchange-demo.demandtrans.com"
//        ));

        config.setAllowedOriginPatterns(Arrays.asList(
                "http://52.45.196.193",
                "http://52.45.196.193:8082",
                "http://tripexchange.com",
                "http://125.99.44.122:9079",
                "http://10.211.55.3:*",
                "http://10.211.55.3:65104",
                "http://34.230.215.239:*",
                "http://localhost:4200",
                "https://localhost:4200",
                "https://*.rtd-denver.com",
                "https://flexride.rtd-denver.com",
                "https://*.demandtrans.com",  // This allows all subdomains
                "https://*.demandtrans-apis.com"  // This allows all subdomains
        ));


        // Add this line to allow credentials
        config.setAllowCredentials(true);

        config.setAllowedHeaders(Arrays.asList(
                "x-requested-with",
                "Content-Type",
                "Origin",
                "X-Requested-With",
                "Accept",
                "Authorization",
                "X-AUTH-TOKEN",
                "X-XSRF-TOKEN"
        ));

        // Add exposed headers to make XSRF-TOKEN available to the frontend
        config.setExposedHeaders(Arrays.asList(
                "X-AUTH-TOKEN",
                "X-XSRF-TOKEN",
                "Set-Cookie"
        ));

        config.setAllowedMethods(Arrays.asList(
                "POST",
                "OPTIONS",
                "GET",
                "PUT",
                "DELETE"
        ));
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public ModelMapper userModelMapper() {
        ModelMapper userModelMapper = new ModelMapper();
        try {
            PropertyMap<User, UserDTO> userDTOmap = new PropertyMap<User, UserDTO>() {
                @Override
                protected void configure() {
                    map().setProviderName(source.getProvider().getProviderName());
                    map().setProviderId(source.getProvider().getProviderId());
                    map().setActive(source.isActive());
                    map().setAccountLocked(source.isAccountLocked());
                    map().setAccountDisabled(source.isAccountDisabled());
                    map().setAccountExpired(source.isAccountExpired());
                    map().setAuthanticationTypeIsAdapter(source.isAuthanticationTypeIsAdapter());
                    map().setPasswordExpired(source.isIsPasswordExpired());
                    map().setNotifyPartnerCreatesTicket(source.isIsNotifyPartnerCreatesTicket());
                    map().setNotifyPartnerUpdateTicket(source.isIsNotifyPartnerUpdateTicket());
                    map().setNotifyClaimedTicketRescinded(source.isIsNotifyClaimedTicketRescinded());
                    map().setNotifyClaimedTicketExpired(source.isIsNotifyClaimedTicketExpired());
                    map().setNotifyNewTripClaimAwaitingApproval(source.isIsNotifyNewTripClaimAwaitingApproval());
                    map().setNotifyNewTripClaimAutoApproved(source.isIsNotifyNewTripClaimAutoApproved());
                    map().setNotifyTripClaimApproved(source.isIsNotifyTripClaimApproved());
                    map().setNotifyTripClaimDeclined(source.isIsNotifyTripClaimDeclined());
                    map().setNotifyTripClaimRescinded(source.isIsNotifyTripClaimRescinded());
                    map().setNotifyTripCommentAdded(source.isIsNotifyTripCommentAdded());
                    map().setNotifyTripResultSubmitted(source.isIsNotifyTripResultSubmitted());
                    map().setNotifyTripReceived(source.isIsNotifyTripReceived());
                    map().setNotifyTripCancelled(source.isIsNotifyTripCancelled());
                    map().setNotifyTripExpired(source.isIsNotifyTripExpired());
                    map().setNotifyTripWeeklyReport(source.isIsNotifyTripWeeklyReport());
                    map().setNotifyTripClaimCancelled(source.isIsNotifyTripClaimCancelled());
                    map().setNotifyTripPriceMismatched(source.isIsNotifyTripPriceMismatched());

                }
            };

            PropertyMap<UserDTO, User> userMap = new PropertyMap<UserDTO, User>() {
                @Override
                protected void configure() {

                    using(ctx -> {
                        var src = (UserDTO) ctx.getSource();
                        return src.getProviderId() > 0 ? new Provider(src.getProviderId()) : null;
                    }).map(source, destination.getProvider());

                    map().setIsActive(source.isActive());
                    map().setAccountLocked(source.isAccountLocked());
                    map().setAccountDisabled(source.isAccountDisabled());
                    map().setAccountExpired(source.isAccountExpired());
                    map().setAuthanticationTypeIsAdapter(source.isAuthanticationTypeIsAdapter());
                    map().setIsPasswordExpired(source.isPasswordExpired());
                    map().setIsNotifyPartnerCreatesTicket(source.isNotifyPartnerCreatesTicket());
                    map().setIsNotifyPartnerUpdateTicket(source.isNotifyPartnerUpdateTicket());
                    map().setIsNotifyClaimedTicketRescinded(source.isNotifyClaimedTicketRescinded());
                    map().setIsNotifyClaimedTicketExpired(source.isNotifyClaimedTicketExpired());
                    map().setIsNotifyNewTripClaimAwaitingApproval(source.isNotifyNewTripClaimAwaitingApproval());
                    map().setIsNotifyNewTripClaimAutoApproved(source.isNotifyNewTripClaimAutoApproved());
                    map().setIsNotifyTripClaimApproved(source.isNotifyTripClaimApproved());
                    map().setIsNotifyTripClaimDeclined(source.isNotifyTripClaimDeclined());
                    map().setIsNotifyTripClaimRescinded(source.isNotifyTripClaimRescinded());
                    map().setIsNotifyTripCommentAdded(source.isNotifyTripCommentAdded());
                    map().setIsNotifyTripResultSubmitted(source.isNotifyTripResultSubmitted());
                    map().setIsNotifyTripReceived(source.isNotifyTripReceived());
                    map().setIsNotifyTripCancelled(source.isNotifyTripCancelled());
                    map().setIsNotifyTripExpired(source.isNotifyTripExpired());
                    map().setIsNotifyTripWeeklyReport(source.isNotifyTripWeeklyReport());
                    map().setIsNotifyTripClaimCancelled(source.isNotifyTripClaimCancelled());
                    map().setIsNotifyTripPriceMismatched(source.isNotifyTripPriceMismatched());
                }
            };


            userModelMapper.addMappings(userDTOmap);
            userModelMapper.addMappings(userMap);
        } catch (Exception e) {
            log.error("Error configuring userModelMapper: " + e.getMessage());
            throw e;
        }
        return userModelMapper;
    }

    @Bean
    public ModelMapper providerModelMapper() {
        PropertyMap<Provider, ProviderDTO> providerDTOmap = new PropertyMap<Provider, ProviderDTO>() {
            @Override
            protected void configure() {
                map().setIsActive(source.isActive());
            }
        };

        PropertyMap<ProviderDTO, Provider> providerMap = new PropertyMap<ProviderDTO, Provider>() {
            @Override
            protected void configure() {
                map().setIsActive(source.isActive());
            }
        };

        ModelMapper providerModelMapper = new ModelMapper();
        providerModelMapper.addMappings(providerMap);
        providerModelMapper.addMappings(providerDTOmap);
        return providerModelMapper;
    }

    @Bean
    public ModelMapper fundingSourceModelMapper() {
        /*customized map for FundingSourceBO - to - FundingSourceDTO*/
        PropertyMap<FundingSource, FundingSourceDTO> fundindSourceDTOmap = new PropertyMap<FundingSource, FundingSourceDTO>() {
            @Override
            protected void configure() {
                map().setStatus(source.isStatus());
            }
        };

        /*customized map for FundingSourceDTO - to - FundingSourceBO*/
        PropertyMap<FundingSourceDTO, FundingSource> fundingSourceMap = new PropertyMap<FundingSourceDTO, FundingSource>() {
            @Override
            protected void configure() {
                map().setStatus(source.isStatus());
                /*end of the outter config*/
            }
        };
        ModelMapper fundingSourceModelMaper = new ModelMapper();
        fundingSourceModelMaper.addMappings(fundingSourceMap);
        fundingSourceModelMaper.addMappings(fundindSourceDTOmap);
        return fundingSourceModelMaper;

    }


    @Bean
    public ModelMapper addressModelMapper() {
        PropertyMap<AddressDTO, Address> addressMap = new PropertyMap<AddressDTO, Address>() {
            @Override
            protected void configure() {
            }
        };

        ModelMapper addressModelMapper = new ModelMapper();
        addressModelMapper.addMappings(addressMap);
        return addressModelMapper;
    }

    @Bean
    public ModelMapper customerModelMapper() {

        /*customized map for CustomerDTO - to - CustomerBO*/
        PropertyMap<CustomerDTO, com.clearinghouse.entity.Customer> customerMap = new PropertyMap<CustomerDTO, com.clearinghouse.entity.Customer>() {
            @Override
            protected void configure() {

                /*end of the outter config*/
            }
        };

        ModelMapper customerModelMapper = new ModelMapper();
        customerModelMapper.addMappings(customerMap);
        return customerModelMapper;
    }


    @Bean
    public ModelMapper providerPartnerModelMapper() {
        PropertyMap<ProviderPartner, ProviderPartnerDTO> providerPartnerDTOmap = new PropertyMap<ProviderPartner, ProviderPartnerDTO>() {
            @Override
            protected void configure() {
                map().setRequesterProviderId(source.getRequesterProvider().getProviderId());
                map().setActive(source.isActive());
                map().setTrustedPartnerForCoordinator(source.isIsTrustedPartnerForCoordinator());
                map().setTrustedPartnerForRequester(source.isIsTrustedPartnerForRequester());
                map().setCoordinatorProviderId(source.getCoordinatorProvider().getProviderId());
                map().setCoordinatorProviderName(source.getCoordinatorProvider().getProviderName());
                map().setRequesterProviderName(source.getRequesterProvider().getProviderName());
            }
        };

        PropertyMap<ProviderPartnerDTO, ProviderPartner> providerPartnerMap = new PropertyMap<ProviderPartnerDTO, ProviderPartner>() {
            @Override
            protected void configure() {
                using(ctx -> {
                    var src = (ProviderPartnerDTO) ctx.getSource();
                    return src.getRequesterProviderId() > 0 ? new Provider(src.getRequesterProviderId()) : null;
                }).map(source, destination.getRequesterProvider());

                using(ctx -> {
                    var src = (ProviderPartnerDTO) ctx.getSource();
                    return src.getCoordinatorProviderId() > 0 ? new Provider(src.getCoordinatorProviderId()) : null;
                }).map(source, destination.getCoordinatorProvider());

                map().setIsActive(source.isActive());
                map().setIsTrustedPartnerForCoordinator(source.isTrustedPartnerForCoordinator());
                map().setIsTrustedPartnerForRequester(source.isTrustedPartnerForRequester());

            }
        };

        ModelMapper providerPartnerModelMapper = new ModelMapper();
        providerPartnerModelMapper.addMappings(providerPartnerMap);
        providerPartnerModelMapper.addMappings(providerPartnerDTOmap);
        return providerPartnerModelMapper;
    }

    @Bean
    public ModelMapper filterModelMapper() {
        PropertyMap<TicketFilter, TicketFilterDTO> ticketFilterDTOmap = new PropertyMap<TicketFilter, TicketFilterDTO>() {
            @Override
            protected void configure() {
                map().setUserId(source.getUser().getId());
                map().setIsActive(source.isActive());
            }
        };

        PropertyMap<TicketFilterDTO, TicketFilter> ticketFilterBOMap = new PropertyMap<TicketFilterDTO, TicketFilter>() {
            @Override
            protected void configure() {

                using(ctx -> {
                    var src = (TicketFilterDTO) ctx.getSource();
                    return src.getUserId() > 0 ? new User(src.getUserId()) : null;
                }).map(source, destination.getUser());
            }
        };

        ModelMapper ticketFilterModelMapper = new ModelMapper();
        ticketFilterModelMapper.addMappings(ticketFilterBOMap);
        ticketFilterModelMapper.addMappings(ticketFilterDTOmap);
        return ticketFilterModelMapper;
    }

    @Bean
    public ModelMapper tripTicketModelMapper() {

        PropertyMap<TripTicket, TripTicketDTO> tripTicketDTOmap = new PropertyMap<TripTicket, TripTicketDTO>() {
            @Override
            protected void configure() {
                /*customized map for TripTicketBO - to - TripTicketrDTO**/

                map().setOriginProviderId(source.getOriginProvider().getProviderId());
                //map(source.getOriginProvider().getProviderId(), destination.getOriginProviderId());

                //map(source.getOriginProvider().getProviderName(), destination.get());

                map().setLastStatusChangedByProviderId(source.getLastStatusChangedByProvider().getProviderId());
                map().setProvisionalProviderId(source.getProvisionalProvider().getProviderId());
                map().setApprovedTripClaimId(source.getApprovedTripClaim().getId());
                map().setCustomerInternalId(source.getCustomerInternalId());

            }
        };

        /*customized map for TripTicketDTO - to - TripTicketrBo*/
        PropertyMap<TripTicketDTO, TripTicket> tripTicketBOMap = new PropertyMap<TripTicketDTO, TripTicket>() {
            @Override
            protected void configure() {

                //  map().getCustomerAddress().setAddressId(source.getCustomer_addressId());
                // map().getPickupAddress().setAddressId(source.getPickupAddress().getAddressId());
                //   map().getDropOffAddress().setAddressId(source.getDropOffAddress().getAddressId());


                using(ctx -> {
                    var src = (TripTicketDTO) ctx.getSource();
                    return src.getOriginProviderId() > 0 ? new Provider(src.getOriginProviderId()) : null;
                }).map(source, destination.getOriginProvider());

                using(ctx -> {
                    var src = (TripTicketDTO) ctx.getSource();
                    return src.getLastStatusChangedByProviderId() != null && src.getLastStatusChangedByProviderId() > 0 ? new Provider(src.getLastStatusChangedByProviderId()) : null;
                }).map(source, destination.getLastStatusChangedByProvider());

                using(ctx -> {
                    var src = (TripTicketDTO) ctx.getSource();
                    return src.getProvisionalProviderId() != null && src.getProvisionalProviderId() > 0 ? new Provider(src.getProvisionalProviderId()) : null;
                }).map(source, destination.getProvisionalProvider());

                using(ctx -> {
                    var src = (TripTicketDTO) ctx.getSource();
                    return src.getStatus() != null && src.getStatus().getStatusId() > 0 ? new Status(src.getStatus().getStatusId()) : null;
                }).map(source, destination.getStatus());




                /*end of the outter config*/
            }
        };

        /*customized map for TripTicket - to - DetailedTripTicketDTO*/
        PropertyMap<TripTicket, DetailedTripTicketDTO> detailedTripTicketDTOmap = new PropertyMap<TripTicket, DetailedTripTicketDTO>() {
            @Override
            protected void configure() {

                map().setOriginProviderId(source.getOriginProvider().getProviderId());
                map().setLastStatusChangedByProviderId(source.getLastStatusChangedByProvider().getProviderId());
                map().setProvisionalProviderId(source.getProvisionalProvider().getProviderId());
                map().setApprovedTripClaimId(source.getApprovedTripClaim().getId());

            }
        };

        PropertyMap<TripClaim, TripClaimDTO> tripTicketTripClaimForToDTOmap = new PropertyMap<TripClaim, TripClaimDTO>() {
            @Override
            protected void configure() {
                map().setClaimantServiceId(source.getService().getServiceId());
                map().setClaimantProviderId(source.getClaimantProvider().getProviderId());
                // Custom handling for claimantProviderName to handle lazy loading properly
                using(ctx -> {
                    TripClaim claim = (TripClaim) ctx.getSource();
                    try {
                        Provider provider = claim.getClaimantProvider();
                        String providerName = provider != null ? provider.getProviderName() : null;
                        //log.debug("ModelMapper tripTicketTripClaimForToDTOmap: claim {} -> providerName: {}", claim.getId(), providerName);
                        return providerName;
                    } catch (Exception e) {
                        log.warn("Failed to access claimantProvider.providerName for claim {}: {}", 
                                claim.getId(), e.getMessage());
                        return null;
                    }
                }).map(source, destination.getClaimantProviderName());
                map().setTripTicketId(source.getTripTicket().getId());
                map().setAckStatus(source.isAckStatus());
                map().setNewRecord(source.isNewRecord());
            }
        };

        PropertyMap<TripTicketComment, TripTicketCommentDTO> tripTicketAndCommentDTOmap = new PropertyMap<TripTicketComment, TripTicketCommentDTO>() {
            @Override
            protected void configure() {

                map().setTripTicketId(source.getTripTicket().getId());
                map().setUserId(source.getUser().getId());

            }
        };

        // Add this mapping to explicitly handle the TripResult's tripTicketId
        PropertyMap<TripResult, TripResultDTO> tripResultDTOExplicitMap = new PropertyMap<TripResult, TripResultDTO>() {
            @Override
            protected void configure() {
                map().setTripTicketId(source.getTripTicket().getId());
            }
        };

        ModelMapper ticketModelMapper = new ModelMapper();

        // Set configuration to be more explicit about mappings
        ticketModelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setAmbiguityIgnored(false);  // Don't ignore ambiguity - fail fast on ambiguous mappings

        ticketModelMapper.addMappings(tripTicketBOMap);
        ticketModelMapper.addMappings(tripTicketDTOmap);
        ticketModelMapper.addMappings(tripTicketTripClaimForToDTOmap);
        ticketModelMapper.addMappings(detailedTripTicketDTOmap);
        ticketModelMapper.addMappings(tripTicketAndCommentDTOmap);
        // Add the explicit mapping
        ticketModelMapper.addMappings(tripResultDTOExplicitMap);

        return ticketModelMapper;

    }

    @Bean
    public ModelMapper tripClaimModelMapper() {
        PropertyMap<TripClaim, TripClaimDTO> tripClaimDTOmap = new PropertyMap<TripClaim, TripClaimDTO>() {
            @Override
            protected void configure() {
                map().setClaimantServiceId(source.getService().getServiceId());
                map().setClaimantProviderId(source.getClaimantProvider().getProviderId());
                // Custom handling for claimantProviderName to handle lazy loading properly
                using(ctx -> {
                    TripClaim claim = (TripClaim) ctx.getSource();
                    try {
                        Provider provider = claim.getClaimantProvider();
                        String providerName = provider != null ? provider.getProviderName() : null;
                        log.debug("ModelMapper tripClaimDTOmap: claim {} -> providerName: {}", 
                                claim.getId(), providerName);
                        return providerName;
                    } catch (Exception e) {
                        log.warn("Failed to access claimantProvider.providerName for claim {}: {}", 
                                claim.getId(), e.getMessage());
                        return null;
                    }
                }).map(source, destination.getClaimantProviderName());
                map().setTripTicketId(source.getTripTicket().getId());
                map().setAckStatus(source.isAckStatus());
                map().setNewRecord(source.isNewRecord());
            }
        };

        PropertyMap<TripClaimDTO, TripClaim> tripClaimBOMap = new PropertyMap<TripClaimDTO, TripClaim>() {
            @Override
            protected void configure() {

                map().setAckStatus(source.isAckStatus());
                map().setNewRecord(source.isNewRecord());

                using(ctx -> {
                    var src = (TripClaimDTO) ctx.getSource();
                    return src.getClaimantServiceId() > 0 ? new Service(src.getClaimantServiceId()) : null;
                }).map(source, destination.getService());


                using(ctx -> {
                    var src = (TripClaimDTO) ctx.getSource();
                    log.debug("Claimant Provider ID: {} for {}", src.getClaimantProviderId(), src);
                    return src.getClaimantProviderId() > 0 ? new Provider(src.getClaimantProviderId()) : null;
                }).map(source, destination.getClaimantProvider());

                using(ctx -> {
                    var src = (TripClaimDTO) ctx.getSource();
                    return src.getTripTicketId() > 0 ? new TripTicket(src.getTripTicketId()) : null;
                }).map(source, destination.getTripTicket());

            }
        };

        ModelMapper tripClaimModelMapper = new ModelMapper();
        tripClaimModelMapper.addMappings(tripClaimBOMap);
        tripClaimModelMapper.addMappings(tripClaimDTOmap);
        return tripClaimModelMapper;
    }

    @Bean
    public ModelMapper tripResultModelMapper() {

        PropertyMap<TripResult, TripResultDTO> tripResultDTOmap = new PropertyMap<TripResult, TripResultDTO>() {
            @Override
            protected void configure() {

                map().setTripTicketId(source.getTripTicket().getId());
                map().setTripClaimId(source.getTripClaim().getId());


            }
        };

        /*customized map for TripClaimDTO - to - TripClaimrBo*/
        PropertyMap<TripResultDTO, TripResult> tripResultBOMap = new PropertyMap<TripResultDTO, TripResult>() {
            @Override
            protected void configure() {


                using(ctx -> {
                    var src = (TripResultDTO) ctx.getSource();
                    return src.getTripClaimId() > 0 ? new TripClaim(src.getTripClaimId()) : null;
                }).map(source, destination.getTripClaim());


                using(ctx -> {
                    var src = (TripResultDTO) ctx.getSource();
                    return src.getTripTicketId() > 0 ? new TripTicket(src.getTripTicketId()) : null;
                }).map(source, destination.getTripTicket());


            }
        };
        ModelMapper tripResultModelMapper = new ModelMapper();
        tripResultModelMapper.addMappings(tripResultBOMap);
        tripResultModelMapper.addMappings(tripResultDTOmap);
        return tripResultModelMapper;

    }

    @Bean
    public ModelMapper tripTicketCommentModelMapper() {
        PropertyMap<TripTicketComment, TripTicketCommentDTO> tripTicketCommentDTOmap = new PropertyMap<TripTicketComment, TripTicketCommentDTO>() {
            @Override
            protected void configure() {
                map().setTripTicketId(source.getTripTicket().getId());
                map().setUserName(source.getUserName());
                map().setUserId(source.getUser().getId());
            }
        };

        PropertyMap<TripTicketCommentDTO, TripTicketComment> tripTicketCommentBOMap = new PropertyMap<TripTicketCommentDTO, TripTicketComment>() {
            @Override
            protected void configure() {

                using(ctx -> {
                    TripTicketCommentDTO src = (TripTicketCommentDTO) ctx.getSource();
                    return src.getTripTicketId() > 0 ? new TripTicket(src.getTripTicketId()) : null;
                }).map(source, destination.getTripTicket());

                using(ctx -> {
                    TripTicketCommentDTO src = (TripTicketCommentDTO) ctx.getSource();
                    return src.getUserId() > 0 ? new User(src.getUserId()) : null;
                }).map(source, destination.getUser());


            }
        };

        // Use conditional mapping to handle possible null values


        ModelMapper tripTicketCommentModelMapper = new ModelMapper();
        tripTicketCommentModelMapper.addMappings(tripTicketCommentBOMap);
        tripTicketCommentModelMapper.addMappings(tripTicketCommentDTOmap);
        return tripTicketCommentModelMapper;
    }

    @Bean
    public ModelMapper adapterLogModelMapper() {

        /*convert from BO to DTO*/
        PropertyMap<com.clearinghouse.entity.AdapterLog, AdapterLogDTO> adapterLogDTOmap = new PropertyMap<com.clearinghouse.entity.AdapterLog, AdapterLogDTO>() {
            @Override
            protected void configure() {

            }
        };

        /*customized map for DTO - to - Bo*/
        PropertyMap<AdapterLogDTO, com.clearinghouse.entity.AdapterLog> adapterLogBOMap = new PropertyMap<AdapterLogDTO, com.clearinghouse.entity.AdapterLog>() {
            @Override
            protected void configure() {

            }
        };
        ModelMapper adapterLogModelMapper = new ModelMapper();
        adapterLogModelMapper.addMappings(adapterLogDTOmap);
        adapterLogModelMapper.addMappings(adapterLogBOMap);
        return adapterLogModelMapper;

    }


    @Bean
    public ModelMapper activityModelMapper() {
        PropertyMap<Activity, ActivityDTO> activityDTOmap = new PropertyMap<Activity, ActivityDTO>() {
            @Override
            protected void configure() {
                map().setTripTicketId(source.getTripTicket().getId());
            }
        };

        PropertyMap<ActivityDTO, Activity> activityBOMap = new PropertyMap<ActivityDTO, Activity>() {
            @Override
            protected void configure() {
                map().getTripTicket().setId(source.getTripTicketId());
            }
        };

        ModelMapper activityModelMapper = new ModelMapper();
        activityModelMapper.addMappings(activityDTOmap);
        activityModelMapper.addMappings(activityBOMap);
        return activityModelMapper;
    }


    @Bean
    public ModelMapper hospitalityModelMapper() {
        PropertyMap<HospitalityAreaProvider, HospitalityAreaProviderDTO> hospitalityAreaDTOmap = new PropertyMap<HospitalityAreaProvider, HospitalityAreaProviderDTO>() {
            @Override
            protected void configure() {
                using(ctx -> {
                    var src = (HospitalityAreaProvider) ctx.getSource();
                    return (src.getService() != null) ? src.getService().getServiceId() : null;
                }).map(source, destination.getServiceId());

                using(ctx -> {
                    var src = (HospitalityAreaProvider) ctx.getSource();
                    return (src.getProvider() != null) ? src.getProvider().getProviderId() : null;
                }).map(source, destination.getProviderId());

                map().setProviderServiceName(source.getProviderServiceName());
                map().setHospitalityProviderId(source.getHospitalityProviderId());
            }
        };

        PropertyMap<HospitalityAreaProviderDTO, HospitalityAreaProvider> hospitalityAreaBOmap = new PropertyMap<HospitalityAreaProviderDTO, HospitalityAreaProvider>() {
            @Override
            protected void configure() {

                using(ctx -> {
                    var src = (HospitalityAreaProviderDTO) ctx.getSource();
                    return src.getServiceId() > 0 ? new Service(src.getServiceId()) : null;
                }).map(source, destination.getService());

                using(ctx -> {
                    var src = (HospitalityAreaProviderDTO) ctx.getSource();
                    return src.getProviderId() > 0 ? new Provider(src.getProviderId()) : null;
                }).map(source, destination.getProvider());

                map().setProviderServiceName(source.getProviderServiceName());
                map().setHospitalityProviderId(source.getHospitalityProviderId());

            }
        };

        ModelMapper hospitalityModelMapper = new ModelMapper();
        hospitalityModelMapper.addMappings(hospitalityAreaDTOmap);
        hospitalityModelMapper.addMappings(hospitalityAreaBOmap);
        return hospitalityModelMapper;
    }


    @Bean
    public ModelMapper serviceModelMapper() {

        PropertyMap<Service, ServiceDTO> serviceDTOmap = new PropertyMap<Service, ServiceDTO>() {
            @Override
            protected void configure() {
                map().setProviderId(source.getProviderId());
                map().setIsActive(source.isActive());
                map().setHospitalityArea(source.isHospitalityArea());
                map().setProviderSelected(source.isProviderSelected());

                using(context -> {
                    Service src = (Service) context.getSource();
                    if (src == null) {
                        return null;
                    }
                    Geometry geometry = src.getServiceAreaGeometry();
                    if (geometry != null) {
                        return geoJSONService.toGeoJSON(geometry);
                    }
                    return null;
                }).map(source, destination.getServiceArea());

                using(ctx -> {
                    var areas = ((Service) ctx.getSource()).getHospitalServiceAreas();
                    log.debug("serviceModelMapper service areas: {}", areas);
                    if (areas == null || areas.isEmpty()) return Collections.emptySet();

                    return areas.stream()
                            .map(area -> serviceareaModelMapper().map(area, ServiceAreaDTO.class))
                            .collect(Collectors.toSet());
                }).map(source, destination.getServiceAreaList());

                using(ctx -> {
                    Object sourceCollection = ctx.getSource();
                    if (sourceCollection == null) {
                        return Collections.<HospitalityAreaProviderDTO>emptySet();
                    }

                    // Un-proxy the Hibernate collection by creating a new HashSet.
                    // This resolves the "PersistentSet to Set" conversion error.
                    Set<HospitalityAreaProvider> providers = new HashSet<>((Collection<HospitalityAreaProvider>) sourceCollection);

                    return providers.stream()
                            .map(p -> {
                                // Defensive checks to prevent NullPointerException
                                Integer serviceId = (p.getService() != null) ? p.getService().getServiceId() : null;
                                Integer providerId = (p.getProvider() != null) ? p.getProvider().getProviderId() : null;

                                return new HospitalityAreaProviderDTO(
                                        p.getHospitalityProviderId(),
                                        serviceId,
                                        providerId,
                                        p.getProviderServiceName());
                            })
                            .collect(Collectors.toSet());
                }).map(source.getHospitalAreaProvider(), destination.getProviderAreaList());


            }
        };

        PropertyMap<ServiceDTO, Service> serviceBOmap = new PropertyMap<ServiceDTO, Service>() {
            @Override
            protected void configure() {
                using(ctx -> {
                    ServiceDTO src = (ServiceDTO) ctx.getSource();
                    if (src == null || src.getProviderId() == 0) {
                        return null;
                    }
                    return new Provider(src.getProviderId());
                }).map(source, destination.getProvider());

                map().setIsActive(source.isActive());
                map().setHospitalityArea(source.isHospitalityArea());
                map().setProviderSelected(source.isProviderSelected());

                using(context -> {
                    ServiceDTO src = (ServiceDTO) context.getSource();
                    if (src == null) {
                        return null;
                    }
                    String serviceArea = src.getServiceArea();
                    if (serviceArea != null && !serviceArea.isEmpty()) {
                        try {
                            return geoJSONService.fromGeoJSON(serviceArea);
                        } catch (Exception e) {
                            log.error("Failed to parse geometry: " + e.getMessage());
                            return null;
                        }
                    }
                    return null;
                }).map(source, destination.getServiceAreaGeometry());

                using(ctx -> {
                    var dtos = ((ServiceDTO) ctx.getSource()).getServiceAreaList();
                    if (dtos == null || dtos.isEmpty()) return Collections.emptySet();
                    return dtos.stream()
                            .map(dto -> serviceareaModelMapper().map(dto, ServiceArea.class))
                            .collect(Collectors.toSet());
                }).map(source, destination.getHospitalServiceAreas());

                using(ctx -> {
                    var areas = ((ServiceDTO) ctx.getSource()).getProviderAreaList();
                    if (areas == null || areas.isEmpty()) return Collections.emptySet();
                    return areas.stream()
                            .map(area -> hospitalityModelMapper().map(area, HospitalityAreaProvider.class))
                            .collect(Collectors.toSet());
                }).map(source, destination.getHospitalAreaProvider());
            }
        };

        ModelMapper serviceModelMapper = new ModelMapper();
        serviceModelMapper.addMappings(serviceDTOmap);
        serviceModelMapper.addMappings(serviceBOmap);
        return serviceModelMapper;
    }


    @Bean
    public ModelMapper serviceareaModelMapper() {
        PropertyMap<ServiceArea, ServiceAreaDTO> serviceareaDTOmap = new PropertyMap<ServiceArea, ServiceAreaDTO>() {
            @Override
            protected void configure() {

                using(ctx -> {
                    ServiceArea src = (ServiceArea) ctx.getSource();
                    return (src.getService() != null) ? src.getService().getServiceId() : null;
                }).map(source, destination.getServiceId());


                using(context -> {
                    var src = (ServiceArea) context.getSource(); // Explicit cast
                    if (src == null) {
                        return null;
                    }
                    var geometry = src.getServiceAreaGeometry();
                    if (geometry != null) {
                        return geoJSONService.toGeoJSON(geometry); // Convert Geometry to WKT (Well-Known Text)
                    }
                    return null;
                }).map(source, destination.getServiceArea());
            }
        };


        PropertyMap<ServiceAreaDTO, ServiceArea> serviceareaBOmap = new PropertyMap<ServiceAreaDTO, ServiceArea>() {
            @Override
            protected void configure() {

                using(ctx -> {
                    var src = (ServiceAreaDTO) ctx.getSource();
                    return src.getServiceId() > 0 ? new Service(src.getServiceId()) : null;
                }).map(source, destination.getService());

                using(context -> {
                    var src = (ServiceAreaDTO) context.getSource(); // Get source explicitly from context
                    if (src == null) {
                        return null;
                    }
                    String serviceArea = src.getServiceArea();
                    log.debug("serviceareaModelMapper dto found serviceArea: " + serviceArea);
                    if (serviceArea != null && !serviceArea.isEmpty()) {
                        try {
                            return geoJSONService.fromGeoJSON(serviceArea);
                        } catch (Exception e) {
                            log.error("Failed to parse geometry: " + e.getMessage());
                            return null;
                        }
                    }
                    return null;
                }).map(source, destination.getServiceAreaGeometry());
            }
        };

        ModelMapper serviceareaModelMapper = new ModelMapper();
        serviceareaModelMapper.addMappings(serviceareaDTOmap);
        serviceareaModelMapper.addMappings(serviceareaBOmap);
        return serviceareaModelMapper;
    }

    //new added by shankar I
    @Bean
    public ModelMapper workingHoursModelMapper() {

        /*convert from BO to DTO*/
        PropertyMap<com.clearinghouse.entity.WorkingHours, WorkingHoursDTO> workinghoursDTOmap = new PropertyMap<com.clearinghouse.entity.WorkingHours, WorkingHoursDTO>() {
            @Override
            protected void configure() {

                map().setProviderId(source.getProvider().getProviderId());
                map().setWorkingHoursId(source.getWorkingHoursId());
                map().setActive(source.getIsActive());
                map().setDay(source.getDay());
                map().setHoliday(source.getIsHoliday());
            }
        };

        /*customized map for DTO - to - Bo*/
        PropertyMap<WorkingHoursDTO, com.clearinghouse.entity.WorkingHours> workinghoursBOmap = new PropertyMap<WorkingHoursDTO, com.clearinghouse.entity.WorkingHours>() {
            @Override
            protected void configure() {

                using(ctx -> {
                    var src = (WorkingHoursDTO) ctx.getSource();
                    return src.getProviderId() > 0 ? new Provider(src.getProviderId()) : null;
                }).map(source, destination.getProvider());

                map().setIsHoliday(source.isHoliday());
                map().setIsActive(source.isActive());

            }
        };
        ModelMapper workinghoursModelMapper = new ModelMapper();
        workinghoursModelMapper.addMappings(workinghoursDTOmap);
        workinghoursModelMapper.addMappings(workinghoursBOmap);
        return workinghoursModelMapper;

    }

    @Bean
    public ModelMapper claimantTripModelMapper() {

        PropertyMap<com.clearinghouse.entity.ClaimantTripTicket, com.clearinghouse.dto.ClaimantTripTicketDTO> ClaimantTripTicketDTOmap = new PropertyMap<com.clearinghouse.entity.ClaimantTripTicket, ClaimantTripTicketDTO>() {
            @Override
            protected void configure() {
                map().setClaimantProvider(source.getClaimantProvider().getProviderId());
                map().setClaimantTripId(source.getClaimantTripId());
                map().setTrip_ticket(source.getTripTicket().getId());

            }
        };

        /*customized map for TripClaimDTO - to - TripClaimrBo*/
        PropertyMap<ClaimantTripTicketDTO, com.clearinghouse.entity.ClaimantTripTicket> ClaimantTripTicketBOMap = new PropertyMap<ClaimantTripTicketDTO, com.clearinghouse.entity.ClaimantTripTicket>() {
            @Override
            protected void configure() {

                using(ctx -> {
                    var src = (ClaimantTripTicketDTO) ctx.getSource();
                    return src.getClaimantProviderId() > 0 ? new Provider(src.getClaimantProviderId()) : null;
                }).map(source, destination.getClaimantProvider());

                map().setClaimantTripId(source.getClaimantTripId());
                map().setTripTicket(new TripTicket(source.getTripTicketId()));

                /*end of the outter config*/
            }
        };
        ModelMapper claimantTripModelMapper = new ModelMapper();
        claimantTripModelMapper.addMappings(ClaimantTripTicketBOMap);
        claimantTripModelMapper.addMappings(ClaimantTripTicketDTOmap);
        return claimantTripModelMapper;

    }


    //new added by shankar I
    @Bean
    public ModelMapper providerCostModelMapper() {

        /*convert from BO to DTO*/
        PropertyMap<com.clearinghouse.entity.ProviderCost, ProviderCostDTO> providerCostDTOmap = new PropertyMap<com.clearinghouse.entity.ProviderCost, ProviderCostDTO>() {

            @Override
            protected void configure() {

                map().setProviderId(source.getProvider().getProviderId());
            }
        };

        /*customized map for DTO - to - Bo*/
        PropertyMap<ProviderCostDTO, com.clearinghouse.entity.ProviderCost> providerCostBOmap = new PropertyMap<ProviderCostDTO, com.clearinghouse.entity.ProviderCost>() {
            @Override
            protected void configure() {

                map().getProvider().setProviderId(source.getProviderId());

            }
        };
        ModelMapper providerCostModelMapper = new ModelMapper();
        providerCostModelMapper.addMappings(providerCostDTOmap);
        providerCostModelMapper.addMappings(providerCostBOmap);
        return providerCostModelMapper;

    }

    //new added by shankar I
    @Bean
    public ModelMapper tripTicketDistanceModelMapper() {

        PropertyMap<com.clearinghouse.entity.TripTicketDistance, TripTicketDistanceDTO> tripTicketDistanceDTOmap = new PropertyMap<com.clearinghouse.entity.TripTicketDistance, TripTicketDistanceDTO>() {
            @Override
            protected void configure() {
                /*customized map for TripTicketDistanceBO - to - TripTicketDistanceDTO*/
                map().setTripTicketId(source.getTripTicket().getId());
            }
        };

        /*customized map for TripTicketDistanceDTO - to - TripTicketDistanceBo*/
        PropertyMap<TripTicketDistanceDTO, com.clearinghouse.entity.TripTicketDistance> tripTicketDistanceBOMap = new PropertyMap<TripTicketDistanceDTO, com.clearinghouse.entity.TripTicketDistance>() {
            @Override
            protected void configure() {

                map().setTripTicket(new TripTicket(source.getTripTicketId()));
                /*end of the outter config*/
            }
        };
        ModelMapper tripTicketDistanceModelMapper = new ModelMapper();
        tripTicketDistanceModelMapper.addMappings(tripTicketDistanceDTOmap);
        tripTicketDistanceModelMapper.addMappings(tripTicketDistanceBOMap);
        return tripTicketDistanceModelMapper;

    }

    @Bean
    public ModelMapper tdsModelMapper() {
        ModelMapper tdsMapper = new ModelMapper();
        tdsMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true);

        // TripRequestType to TripTicketDTO mapping
        PropertyMap<TripRequestType, TripTicketDTO> tripRequestMap = new PropertyMap<TripRequestType, TripTicketDTO>() {
            @Override
            protected void configure() {
                // Address mappings - create new objects to avoid null issues
                using(ctx -> {
                    AddressType src = source.getPickupAddress();
                    if (src == null) return null;
                    AddressDTO dto = new AddressDTO();
                    if (src.getStreet() != null) {
                        String street1 = (src.getStreetNo() != null ? src.getStreetNo() + " " : "") + src.getStreet();
                        dto.setStreet1(street1);
                    }
                    dto.setCity(src.getCommunity());
                    dto.setZipcode(src.getPostalNo());
                    dto.setCounty(src.getLocation());
                    dto.setState(src.getCountry());
                    if (src.getGeographicLocation() != null) {
                        dto.setLatitude(src.getGeographicLocation().getLatitude());
                        dto.setLongitude(src.getGeographicLocation().getLongitude());
                    }
                    dto.setCommonName(src.getAddressName());
                    return dto;
                }).map(source, destination.getPickupAddress());

                using(ctx -> {
                    AddressType src = source.getDropoffAddress();
                    if (src == null) return null;
                    AddressDTO dto = new AddressDTO();
                    if (src.getStreet() != null) {
                        String street1 = (src.getStreetNo() != null ? src.getStreetNo() + " " : "") + src.getStreet();
                        dto.setStreet1(street1);
                    }
                    dto.setCity(src.getCommunity());
                    dto.setZipcode(src.getPostalNo());
                    dto.setCounty(src.getLocation());
                    dto.setState(src.getCountry());
                    if (src.getGeographicLocation() != null) {
                        dto.setLatitude(src.getGeographicLocation().getLatitude());
                        dto.setLongitude(src.getGeographicLocation().getLongitude());
                    }
                    dto.setCommonName(src.getAddressName());
                    return dto;
                }).map(source, destination.getDropOffAddress());

                // Time mappings
                using(ctx -> {
                    com.clearinghouse.tds.generated.model.Time src = source.getPickupTime();
                    if (src == null || src.getTime() == null) return null;
                    return new java.sql.Time(src.getTime().toGregorianCalendar().getTimeInMillis());
                }).map(source, destination.getRequestedPickupTime());

                using(ctx -> {
                    com.clearinghouse.tds.generated.model.Time src = source.getDropoffTime();
                    if (src == null || src.getTime() == null) return null;
                    return new java.sql.Time(src.getTime().toGregorianCalendar().getTimeInMillis());
                }).map(source, destination.getRequestedDropOffTime());

                using(ctx -> {
                    com.clearinghouse.tds.generated.model.Time src = source.getAppointmentTime();
                    if (src == null || src.getTime() == null) return null;
                    return new java.sql.Time(src.getTime().toGregorianCalendar().getTimeInMillis());
                }).map(source, destination.getAppointmentTime());

                using(ctx -> {
                    com.clearinghouse.tds.generated.model.Time src = source.getPickupWindowStartTime();
                    if (src == null || src.getTime() == null) return null;
                    return new java.sql.Time(src.getTime().toGregorianCalendar().getTimeInMillis());
                }).map(source, destination.getEarliestPickupTime());

                // Other fields
                using(ctx -> source.getSpecialAttributes() != null ?
                        String.join(",", source.getSpecialAttributes()) : null)
                        .map(source, destination.getCustomerMobilityFactors());

                using(ctx -> source.getTransportServices() != null ?
                        String.join(",", source.getTransportServices()) : null)
                        .map(source, destination.getServiceLevel());

                // Boolean fields
                map().setIsTripIsolation(source.getDetoursPermissible());
                map().setOutsideCoreHours(source.getHardConstraintOnPickupTime());
            }
        };

        // TripTicketDTO to TripRequestResponseType mapping
        PropertyMap<TripTicketDTO, TripRequestResponseType> responseMap = new PropertyMap<TripTicketDTO, TripRequestResponseType>() {
            @Override
            protected void configure() {
                // Map addresses
                using(ctx -> {
                    AddressDTO src = source.getPickupAddress();
                    if (src == null) return null;
                    AddressType dst = new AddressType();

                    // Handle street and street number
                    if (src.getStreet1() != null) {
                        String[] parts = src.getStreet1().split(" ", 2);
                        if (parts.length > 1) {
                            try {
                                dst.setStreetNo(new BigInteger(parts[0]));
                                dst.setStreet(parts[1]);
                            } catch (NumberFormatException e) {
                                dst.setStreet(src.getStreet1());
                            }
                        } else {
                            dst.setStreet(src.getStreet1());
                        }
                    }

                    dst.setCommunity(src.getCity());
                    dst.setPostalNo(src.getZipcode());
                    dst.setLocation(src.getCounty());
                    dst.setCountry(src.getState());
                    dst.setAddressName(src.getCommonName());

                    // Set geo location if lat/long available
                    if (src.getLatitude() != 0 || src.getLongitude() != 0) {
                        GeographicLocation geo = new GeographicLocation();
                        geo.setLatitude(src.getLatitude());
                        geo.setLongitude(src.getLongitude());
                        dst.setGeographicLocation(geo);
                    }

                    return dst;
                }).map(source, destination.getScheduledPickupPoint());

                using(ctx -> {
                    AddressDTO src = source.getDropOffAddress();
                    if (src == null) return null;
                    AddressType dst = new AddressType();

                    // Handle street and street number
                    if (src.getStreet1() != null) {
                        String[] parts = src.getStreet1().split(" ", 2);
                        if (parts.length > 1) {
                            try {
                                dst.setStreetNo(new BigInteger(parts[0]));
                                dst.setStreet(parts[1]);
                            } catch (NumberFormatException e) {
                                dst.setStreet(src.getStreet1());
                            }
                        } else {
                            dst.setStreet(src.getStreet1());
                        }
                    }

                    dst.setCommunity(src.getCity());
                    dst.setPostalNo(src.getZipcode());
                    dst.setLocation(src.getCounty());
                    dst.setCountry(src.getState());
                    dst.setAddressName(src.getCommonName());

                    // Set geo location if lat/long available
                    if (src.getLatitude() != 0 || src.getLongitude() != 0) {
                        GeographicLocation geo = new GeographicLocation();
                        geo.setLatitude(src.getLatitude());
                        geo.setLongitude(src.getLongitude());
                        dst.setGeographicLocation(geo);
                    }

                    return dst;
                }).map(source, destination.getScheduledDropoffPoint());

                // Map time
                using(ctx -> {
                    java.sql.Time src = source.getRequestedPickupTime();
                    if (src == null) return null;
                    com.clearinghouse.tds.generated.model.Time dst = new com.clearinghouse.tds.generated.model.Time();
                    try {
                        GregorianCalendar gc = new GregorianCalendar();
                        gc.setTimeInMillis(src.getTime());
                        dst.setTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc));
                        return dst;
                    } catch (DatatypeConfigurationException e) {
                        throw new RuntimeException("Error converting time", e);
                    }
                }).map(source, destination.getScheduledPickupTime());

                // Map availability based on status
                using(ctx -> {
                    StatusDTO status = source.getStatus();
                    if (status == null) return false;
                    // Add your logic to determine availability based on status
                    // For now, returning true as a placeholder
                    return true;
                }).map(source, destination.getTripAvailable());

                // Map service level to transport services if present
                using(ctx -> {
                    String serviceLevel = source.getServiceLevel();
                    if (serviceLevel == null || serviceLevel.isEmpty()) return null;
                    return Arrays.asList(serviceLevel.split(","));
                }).map(source, destination.getTransportServices());
            }
        };

        // Add mappings to the mapper
        tdsMapper.addMappings(tripRequestMap);
        tdsMapper.addMappings(responseMap);

        return tdsMapper;
    }

    private Boolean isStatusAvailable(StatusDTO status) {
        return status != null;
        // Add your status availability logic here
    }

    @Bean
    public SimpleMailMessage simpleMailMessage() {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        String emailFrom = applicationSettingDAO.findApplicationSettingById(1).getFromEmail();

        if (emailFrom.equalsIgnoreCase("")) {
            emailFrom = emailFrom;
        }

        simpleMailMessage.setFrom(emailFrom);
        simpleMailMessage.setTo(emailTo);
        simpleMailMessage.setSubject(emailSubject);
        return simpleMailMessage;
    }

    @Bean
    public MimeMessage mimeMessage() {
        Properties properties = new Properties();
        Session session = Session.getDefaultInstance(properties);
        return new MimeMessage(session);
    }

    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailHost);
        mailSender.setPort(Integer.parseInt(emailPort));
        mailSender.setUsername(emailUserName.trim());
        mailSender.setPassword(emailPassword);
//        byte[] valueDecoded = Base64.getDecoder().decode(applicationSettingDAO.findApplicationSettingById(1).getPasswordOfMail().getBytes());
//        String decodedPassword = new String(valueDecoded);
//
//        String password = decodedPassword;
//        if (password.equalsIgnoreCase("")) {
//            password = emailPassword;
//        }


        mailSender.setJavaMailProperties(getMailProperties());
        return mailSender;
    }

    private Properties getMailProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", emailTransportProtocol);
        properties.setProperty("mail.smtp.auth", emailSmtpAuth);
        properties.setProperty("mail.smtp.starttls.enable", emailSmtpStarttlsEnable);
        return properties;
    }


    @Bean
    @Primary
    public FreeMarkerConfigurationFactoryBean getFreeMarkerConfiguration() {
        FreeMarkerConfigurationFactoryBean bean = new FreeMarkerConfigurationFactoryBean();
        bean.setTemplateLoaderPath("classpath:/notification-templates/");
        return bean;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


}
