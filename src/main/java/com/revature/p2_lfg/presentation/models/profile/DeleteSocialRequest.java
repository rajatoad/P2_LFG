package com.revature.p2_lfg.presentation.models.profile;

import lombok.*;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeleteSocialRequest extends CreateSocialRequest{
}
