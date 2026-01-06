package com.fba.logi.agent.skill.marketing;

import com.fba.logi.agent.skill.*;
import com.fba.logi.domain.marketing.service.IRaffleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 执行抽奖 Skill
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecuteRaffleSkill extends AbstractSkill {

    private final IRaffleService raffleService;
    private final Random random = new Random();

    @Override
    public String getSkillId() {
        return "execute_raffle";
    }

    @Override
    public String getSkillName() {
        return "执行抽奖";
    }

    @Override
    public String getDescription() {
        return "为用户执行一次抽奖，根据活动策略返回抽奖结果";
    }

    @Override
    public String getDomain() {
        return "marketing";
    }

    @Override
    public SkillParameterSchema getParameterSchema() {
        return SkillParameterSchema.builder()
                .parameters(List.of(
                        SkillParameterSchema.ParameterDefinition.integerParam(
                                "activityId", "活动ID"),
                        SkillParameterSchema.ParameterDefinition.stringParam(
                                "userId", "参与抽奖的用户ID")
                ))
                .required(List.of("activityId", "userId"))
                .build();
    }

    @Override
    protected SkillResult doExecute(SkillContext context, Map<String, Object> parameters) {
        long activityId = getRequiredInt(parameters, "activityId");
        String userId = getOptionalString(parameters, "userId", context.getUserId());

        log.info("用户 {} 参与活动 {} 抽奖", userId, activityId);

        // TODO: 调用真实的抽奖服务
        // RaffleResult result = raffleService.performRaffleWithActivity(activityId, userId);

        // 模拟抽奖结果
        boolean isWinner = random.nextDouble() < 0.3; // 30% 中奖率

        if (isWinner) {
            String[] prizes = {"优惠券50元", "免运费券", "积分x100", "iPhone 16 Pro"};
            String prize = prizes[random.nextInt(prizes.length)];

            return SkillResult.success(
                    String.format("🎉 恭喜您中奖了！奖品: %s", prize),
                    Map.of(
                            "won", true,
                            "prize", prize,
                            "activityId", activityId,
                            "userId", userId
                    )
            );
        } else {
            return SkillResult.success(
                    "很遗憾，本次未中奖。感谢您的参与，祝下次好运！",
                    Map.of(
                            "won", false,
                            "activityId", activityId,
                            "userId", userId
                    )
            );
        }
    }
}
