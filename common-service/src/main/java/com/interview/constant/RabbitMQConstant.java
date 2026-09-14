package com.interview.constant;

public interface RabbitMQConstant {

        interface Exchange{
            /*课程有关的交换机*/
            String COURSE_EXCHANGE = "course.topic";
            /*简历有关交换机*/
            String RESUME_EXCHANGE = "resume.topic";
            /*订单有关的交换机*/
            String ORDER_EXCHANGE = "order.topic";

            /*学习有关的交换机*/
            String LEARNING_EXCHANGE = "learning.topic";

            /*信息中心短信相关的交换机*/
            String SMS_EXCHANGE = "sms.direct";

            /*异常信息的交换机*/
            String ERROR_EXCHANGE = "error.topic";

            /*支付有关的交换机*/
            String PAY_EXCHANGE = "pay.topic";
            /*交易服务延迟任务交换机*/
            String TRADE_DELAY_EXCHANGE = "trade.delay.topic";

            /*点赞记录有关的交换机*/
            String LIKE_RECORD_EXCHANGE = "like.record.topic";

            String PROMOTION_EXCHANGE = "promotion.topic";
        }
        interface Queue {
            String ERROR_QUEUE_TEMPLATE = "error.{}.queue";
            String resume_parse_queue= "resume.parse.queue";
            String RESUME_DELETE_QUEUE = "resume.delete.queue";
        }
        interface Key{
            /*课程有关的 RoutingKey*/
            String COURSE_NEW_KEY = "course.new";
            String COURSE_UP_KEY = "course.up";
            String COURSE_DOWN_KEY = "course.down";
            String COURSE_EXPIRE_KEY = "course.expire";
            String COURSE_DELETE_KEY = "course.delete";
            String RESUME_DELETE_KEY = "resume.delete";
            String ORDER_DELETE_KEY = "order.delete";
            /*订单有关的RoutingKey*/
            String ORDER_PAY_KEY = "order.pay";
            String ORDER_REFUND_KEY = "order.refund";
            String RESUME_PARSE_LEY="resume.parse";
            /*积分相关RoutingKey*/
            /* 写回答 */
            String WRITE_REPLY = "reply.new";
            /* 签到 */
            String SIGN_IN = "sign.in";
            /* 学习视频 */
            String LEARN_SECTION = "section.learned";
            /* 写笔记 */
            String WRITE_NOTE = "note.new";
            /* 笔记被采集 */
            String NOTE_GATHERED = "note.gathered";

            /*点赞的RoutingKey*/
            String LIKED_TIMES_KEY_TEMPLATE = "{}.times.changed";
            /*问答*/
            String QA_LIKED_TIMES_KEY = "QA.times.changed";
            /*笔记*/
            String NOTE_LIKED_TIMES_KEY = "NOTE.times.changed";
            //        评论
            String COMMENT_LIKED_TIMES_KEY = "COMMENT.times.changed";
            /*短信系统发送短信*/
            String SMS_MESSAGE = "sms.message";

            /*异常RoutingKey的前缀*/
            String ERROR_KEY_PREFIX = "error.";
            String DEFAULT_ERROR_KEY = "error.#";

            /*支付有关的key*/
            String PAY_SUCCESS = "pay.success";
            String REFUND_CHANGE = "refund.status.change";

            String ORDER_DELAY_KEY = "delay.order.query";

            String COUPON_RECEIVE ="coupon.receive";
        }


}
