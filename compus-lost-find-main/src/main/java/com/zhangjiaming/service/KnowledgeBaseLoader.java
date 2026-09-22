//package com.zhangjiaming.service;
//
//import org.springframework.ai.document.Document;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * 知识库加载器：应用启动时将平台 FAQ 文档向量化后写入向量库（pgvector）。
// */
//@Component
//public class KnowledgeBaseLoader implements CommandLineRunner {
//
//    private final VectorStore vectorStore;
//
//    public KnowledgeBaseLoader(VectorStore vectorStore) {
//        this.vectorStore = vectorStore;
//    }
//
//    @Override
//    public void run(String... args) {
//        // 幂等：先清理旧的 FAQ 文档，再重新入库，避免重复
//        vectorStore.delete("source == 'faq'");
//        vectorStore.add(buildDocuments());
//    }
//
//    /**
//     * 构建知识库文档（平台使用 FAQ）
//     */
//    private List<Document> buildDocuments() {
//        Map<String, Object> meta = Map.of("source", "faq");
//        return List.of(
//                new Document("校园失物招领平台是一个帮助校内师生发布和查找丢失或捡到物品的公益平台，"
//                        + "同学们可以在平台上发布失物招领信息、浏览他人发布的信息，从而快速找回遗失物品或归还捡到的物品。", meta),
//                new Document("发布物品信息前需要先注册并登录平台。发布时需填写物品类型、发布类型（丢失或捡到）、标题、详细描述、"
//                        + "物品特征、丢失或捡到的地点、事件发生时间以及联系方式。填写越详细，越有助于快速找到物品。", meta),
//                new Document("平台的物品分类共六种：1-证件，2-钥匙，3-电子设备，4-衣物，5-钱包，6-其他。"
//                        + "发布物品时请选择合适的分类，方便他人查找。", meta),
//                new Document("查找物品时，可以在物品列表页通过关键词、物品类型、发布类型（丢失或捡到）和状态（未找回或已找回）进行筛选搜索。"
//                        + "关键词支持搜索标题、描述、特征和地点。", meta),
//                new Document("如果看到自己丢失的物品或想归还捡到的物品，可以进入物品详情页查看发布者的联系方式，直接与对方沟通协商归还事宜。", meta),
//                new Document("物品找回或归还完成后，请及时登录平台，在物品详情或列表中把物品状态更新为「已找回/已归还」，避免他人继续联系。", meta),
//                new Document("安全提醒：请勿发布虚假信息；贵重物品（如证件、手机、钱包）建议同时联系学校保卫处；"
//                        + "线下交接物品时注意人身和财产安全，避免泄露过多个人隐私信息。", meta),
//                new Document("如在使用平台过程中遇到问题，或需要举报违规信息，请联系平台管理员，或查看平台首页的公告通知了解最新说明。", meta));
//    }
//}
