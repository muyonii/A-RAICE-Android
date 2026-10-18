package com.example.a_raiceandrd

import com.example.a_raiceandrd.data.repository.api.AlertDto
import com.example.a_raiceandrd.data.repository.api.AuthResponse
import com.example.a_raiceandrd.data.repository.api.DiseaseDto
import com.example.a_raiceandrd.data.repository.api.MapResponseDto
import com.example.a_raiceandrd.data.repository.api.OutbreakDto
import com.example.a_raiceandrd.data.repository.api.Page
import com.example.a_raiceandrd.data.repository.api.ReportDto
import com.example.a_raiceandrd.data.repository.api.ScanDto
import com.example.a_raiceandrd.data.repository.api.UserDto
import com.example.a_raiceandrd.ui.auth.FLogin
import com.example.a_raiceandrd.ui.auth.Role
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiModelsDeserializationTest {

    private val gson = Gson()

    @Test
    fun testAuthResponseFarmerDeserialization() {
        val json = """
            {
                "token": "1|secret_token_12345",
                "user": {
                    "id": 10,
                    "name": "Farmer Juan",
                    "email": "juan@example.com",
                    "role": "farmer",
                    "farmer_profile": {
                        "id": 5,
                        "barangay": "Gabon",
                        "municipality": "abucay",
                        "contact_number": "09171234567",
                        "farm_lat": 14.7321,
                        "farm_long": 120.5342
                    }
                }
            }
        """.trimIndent()

        val auth = gson.fromJson(json, AuthResponse::class.java)
        assertEquals("1|secret_token_12345", auth.token)
        assertNotNull(auth.user)
        assertEquals("Farmer Juan", auth.user?.name)
        assertEquals("farmer", auth.user?.role)
        assertEquals(Role.FARMER, FLogin.mapRole(auth.user?.role))
        assertEquals(14.7321, auth.user?.farmerProfile?.farmLat ?: 0.0, 0.0001)
        assertEquals(120.5342, auth.user?.farmerProfile?.farmLong ?: 0.0, 0.0001)
    }

    @Test
    fun testRolesMapping() {
        assertEquals(Role.ADMIN, FLogin.mapRole("admin"))
        assertEquals(Role.ADMIN, FLogin.mapRole("lgu_staff"))
        assertEquals(Role.ADMIN, FLogin.mapRole("ADMIN"))
        assertEquals(Role.ADMIN, FLogin.mapRole("LGU_STAFF"))
        assertEquals(Role.FARMER, FLogin.mapRole("farmer"))
        assertEquals(Role.FARMER, FLogin.mapRole("FARMER"))
        assertNull(FLogin.mapRole("unknown_role"))
        assertNull(FLogin.mapRole(null))
    }

    @Test
    fun testScansPageDeserialization() {
        val json = """
            {
                "current_page": 1,
                "data": [
                    {
                        "id": 101,
                        "status": "completed",
                        "created_at": "2026-10-18T10:30:00Z",
                        "image_url": "https://storage.railway.app/scans/101.jpg",
                        "confidence": 0.942,
                        "disease": {
                            "id": 1,
                            "name": "Bacterial Leaf Blight"
                        },
                        "top_3": [
                            {"name": "Bacterial Leaf Blight", "confidence": 0.942},
                            {"name": "Bacterial Leaf Streak", "confidence": 0.041},
                            {"name": "Brown Spot", "confidence": 0.017}
                        ],
                        "farmer": {
                            "id": 5,
                            "name": "Maria Clara",
                            "contact_number": "09187654321"
                        }
                    }
                ],
                "last_page": 3,
                "total": 45,
                "per_page": 15
            }
        """.trimIndent()

        val type = object : TypeToken<Page<ScanDto>>() {}.type
        val page: Page<ScanDto> = gson.fromJson(json, type)

        assertEquals(1, page.currentPage)
        assertEquals(3, page.lastPage)
        assertEquals(45, page.total)
        assertEquals(1, page.data?.size)

        val scan = page.data!!.first()
        assertEquals(101, scan.id)
        assertEquals("completed", scan.status)
        assertEquals("Bacterial Leaf Blight", scan.disease?.name)
        assertEquals(3, scan.top3?.size)
        assertEquals(0.942, scan.confidence ?: 0.0, 0.001)
        assertEquals("Maria Clara", scan.farmer?.name)
    }

    @Test
    fun testAlertsDeserialization() {
        val json = """
            {
                "current_page": 1,
                "data": [
                    {
                        "id": 42,
                        "message": "High alert: Tungro detected in Orion",
                        "severity": "high",
                        "outbreak_id": 12,
                        "created_at": "2026-10-18T08:00:00Z",
                        "read_at": null
                    }
                ],
                "total": 1
            }
        """.trimIndent()

        val type = object : TypeToken<Page<AlertDto>>() {}.type
        val page: Page<AlertDto> = gson.fromJson(json, type)

        val alert = page.data!!.first()
        assertEquals(42, alert.id)
        assertEquals("high", alert.severity)
        assertNull(alert.readAt)
    }

    @Test
    fun testMapResponseDeserialization() {
        val json = """
            {
                "municipalities": [
                    {"slug": "abucay", "severity": "medium"},
                    {"slug": "balanga", "severity": "high"}
                ],
                "outbreaks": [
                    {
                        "id": 7,
                        "disease": "Rice Blast",
                        "municipality": "balanga",
                        "severity": "high",
                        "created_at": "2026-10-15T00:00:00Z"
                    }
                ],
                "farms": [
                    {
                        "id": 3,
                        "barangay": "Cupang",
                        "municipality": "balanga",
                        "lat": 14.68,
                        "lng": 120.54,
                        "farmer": {
                            "id": 12,
                            "name": "Pedro",
                            "contact_number": "09201112233"
                        },
                        "scan": {
                            "id": 99,
                            "disease": "Rice Blast",
                            "scan_date": "2026-10-17"
                        }
                    }
                ]
            }
        """.trimIndent()

        val map = gson.fromJson(json, MapResponseDto::class.java)
        assertEquals(2, map.municipalities?.size)
        assertEquals("abucay", map.municipalities?.first()?.slug)
        assertEquals(1, map.outbreaks?.size)
        assertEquals("Rice Blast", map.outbreaks?.first()?.disease)
        assertEquals(1, map.farms?.size)
        val farm = map.farms!!.first()
        assertEquals("Pedro", farm.farmer?.name)
        assertEquals("Rice Blast", farm.scan?.disease)
    }

    @Test
    fun testReportDtoDeserialization() {
        val json = """
            {
                "id": 15,
                "municipality": "balanga",
                "range_start": "2026-10-01",
                "range_end": "2026-10-18",
                "summary_data": {
                    "total_scans": 80,
                    "confirmed_cases": 65,
                    "top_disease": "Bacterial Leaf Blight",
                    "per_disease": [
                        {"disease": "Bacterial Leaf Blight", "count": 40},
                        {"disease": "Rice Blast", "count": 25}
                    ]
                },
                "generated_by": {
                    "id": 1,
                    "name": "Provincial Agriculturist"
                },
                "created_at": "2026-10-18T12:00:00Z"
            }
        """.trimIndent()

        val report = gson.fromJson(json, ReportDto::class.java)
        assertEquals(15, report.id)
        assertEquals("balanga", report.municipality)
        assertEquals(80, report.summaryData?.totalScans)
        assertEquals(65, report.summaryData?.confirmedCases)
        assertEquals("Bacterial Leaf Blight", report.summaryData?.topDisease)
        assertEquals(2, report.summaryData?.perDisease?.size)
        assertEquals("Provincial Agriculturist", report.generatedBy?.name)
    }

    @Test
    fun testOutbreakDtoDeserialization() {
        val json = """
            {
                "id": 9,
                "disease": {
                    "id": 4,
                    "name": "Sheath Blight"
                },
                "municipality": "orani",
                "status": "resolved",
                "severity": "medium",
                "scans_count": 18,
                "created_at": "2026-10-10",
                "closed_at": "2026-10-16",
                "closed_by": {
                    "id": 2,
                    "name": "Admin Officer"
                }
            }
        """.trimIndent()

        val outbreak = gson.fromJson(json, OutbreakDto::class.java)
        assertEquals(9, outbreak.id)
        assertEquals("resolved", outbreak.status)
        assertEquals(18, outbreak.scansCount)
        assertEquals("Admin Officer", outbreak.closedBy?.name)
    }
}
